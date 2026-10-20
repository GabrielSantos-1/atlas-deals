package com.atlasdeals.publisher.infrastructure.messaging;

import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.events.ContentGeneratedEvent;
import com.atlasdeals.publisher.domain.model.ProcessedEvent;
import com.atlasdeals.publisher.domain.model.Publication;
import com.atlasdeals.publisher.infrastructure.config.RabbitMQConfig;
import com.atlasdeals.publisher.infrastructure.persistence.ProcessedEventRepository;
import com.atlasdeals.publisher.infrastructure.persistence.PublicationRepository;
import org.aopalliance.intercept.MethodInterceptor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the idempotency barrier across the real Postgres/RabbitMQ boundary.
 *
 * <p>Messages are published to the broker and consumed by the real listener container, so
 * redelivery in the failure scenario is performed by RabbitMQ itself, not by calling the
 * handler twice from the test.</p>
 */
@Testcontainers
@SpringBootTest
@Import(PublisherEventConsumerIdempotencyTest.ListenerFailureConfig.class)
class PublisherEventConsumerIdempotencyTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("atlasdeals")
        .withUsername("atlas")
        .withPassword("atlas");

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(
        DockerImageName.parse("rabbitmq:3.13-management"))
        .withExchange("atlasdeals.ai.exchange", "direct", false, false, true, Map.of())
        .withExchange("atlasdeals.image.exchange", "direct", false, false, true, Map.of());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        registry.add("spring.rabbitmq.virtual-host", () -> "/");

        // Keep the scheduled pollers out of the way: they would pick up the rows created here.
        registry.add("publisher.queue-poll-interval-ms", () -> "86400000");
        registry.add("publisher.schedule-poll-interval-ms", () -> "86400000");
        registry.add("publisher.retry-interval-ms", () -> "86400000");
    }

    @org.springframework.beans.factory.annotation.Autowired
    private RabbitTemplate rabbitTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    private AmqpAdmin amqpAdmin;

    @org.springframework.beans.factory.annotation.Autowired
    private PublicationRepository publicationRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void resetState() {
        ListenerFailureConfig.DELIVERIES.set(0);
        ListenerFailureConfig.FAIL_AFTER_COMMIT.set(false);
        amqpAdmin.purgeQueue(RabbitMQConfig.CONTENT_GENERATED_QUEUE);
    }

    @Test
    void duplicateDeliveryOfTheSameEventCreatesOnlyOnePublication() {
        UUID productId = UUID.randomUUID();
        ContentGeneratedEvent event = newEvent(productId);

        rabbitTemplate.convertAndSend(RabbitMQConfig.CONTENT_GENERATED_QUEUE, event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.CONTENT_GENERATED_QUEUE, event);

        Awaitility.await().atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> assertThat(ListenerFailureConfig.DELIVERIES.get())
                .isGreaterThanOrEqualTo(2));

        assertThat(publicationRepository.findByProductIdOrderByCreatedAtDesc(productId))
            .as("the business effect must be applied exactly once")
            .hasSize(1);
        assertThat(processedEventRepository.findById(event.getEventId()))
            .as("the event must be recorded for auditing")
            .isPresent();
    }

    @Test
    void redeliveryAfterCommitAndCrashBeforeAckDoesNotDuplicateTheEffect() {
        UUID productId = UUID.randomUUID();
        ContentGeneratedEvent event = newEvent(productId);

        // The first delivery commits the marker + publication, then fails before the ack, so
        // RabbitMQ rejects and requeues the message. The second delivery must be a no-op.
        ListenerFailureConfig.FAIL_AFTER_COMMIT.set(true);

        rabbitTemplate.convertAndSend(RabbitMQConfig.CONTENT_GENERATED_QUEUE, event);

        Awaitility.await().atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> assertThat(ListenerFailureConfig.DELIVERIES.get())
                .as("the broker must deliver the message again after the failure")
                .isGreaterThanOrEqualTo(2));

        assertThat(ListenerFailureConfig.FAIL_AFTER_COMMIT)
            .as("the injected failure must have happened exactly once")
            .isFalse();
        assertThat(publicationRepository.findByProductIdOrderByCreatedAtDesc(productId))
            .as("the redelivery must not duplicate the committed business effect")
            .hasSize(1);

        List<ProcessedEvent> markers = processedEventRepository.findAll().stream()
            .filter(row -> row.getEventId().equals(event.getEventId()))
            .toList();
        assertThat(markers)
            .as("the redelivery must be recognized as already processed")
            .hasSize(1);
    }

    @Test
    void processedEventRowIsPersistedForAudit() {
        UUID productId = UUID.randomUUID();
        ContentGeneratedEvent event = newEvent(productId);

        rabbitTemplate.convertAndSend(RabbitMQConfig.CONTENT_GENERATED_QUEUE, event);

        Awaitility.await().atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                ProcessedEvent row = processedEventRepository.findById(event.getEventId())
                    .orElse(null);
                assertThat(row).isNotNull();
                assertThat(row.getConsumer()).isEqualTo(PublisherEventConsumer.CONTENT_CONSUMER);
                assertThat(row.getProcessedAt()).isNotNull();
            });
    }

    private ContentGeneratedEvent newEvent(UUID productId) {
        return new ContentGeneratedEvent(
            ProductId.of(productId),
            Map.of("site", "Content for " + productId),
            "Deal title",
            "Deal description",
            "Deal summary",
            "SEO description",
            "#deal",
            "Electronics",
            "deal,offer",
            "HIGH",
            "Technical summary",
            "Pros",
            "Cons",
            "Deal hunter",
            null,
            "Example Store",
            new BigDecimal("199.90"),
            new BigDecimal("299.90")
        );
    }

    @TestConfiguration
    static class ListenerFailureConfig {

        static final AtomicInteger DELIVERIES = new AtomicInteger();
        static final AtomicBoolean FAIL_AFTER_COMMIT = new AtomicBoolean(false);

        /**
         * Wraps the listener container invocation: {@code proceed()} runs the handler and
         * commits its transaction, and only then the advice throws. That is exactly the
         * "commit succeeded, ack never happened" window described in the review.
         */
        @Bean
        static BeanPostProcessor listenerFailureAdvicePostProcessor() {
            return new BeanPostProcessor() {
                @Override
                public Object postProcessAfterInitialization(Object bean, String beanName) {
                    if (bean instanceof SimpleRabbitListenerContainerFactory factory) {
                        factory.setAdviceChain((MethodInterceptor) invocation -> {
                            DELIVERIES.incrementAndGet();
                            Object result = invocation.proceed();
                            if (FAIL_AFTER_COMMIT.compareAndSet(true, false)) {
                                throw new IllegalStateException(
                                    "Simulated process crash after commit and before broker ack");
                            }
                            return result;
                        });
                    }
                    return bean;
                }
            };
        }
    }
}
