package com.atlasdeals.publisher.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PUBLISHER_EXCHANGE = "atlasdeals.publisher.exchange";
    public static final String CONTENT_GENERATED_QUEUE = "content.generated.q";
    public static final String IMAGE_GENERATED_QUEUE = "image.generated.q";
    public static final String PUBLISHER_DLQ = "publisher.dlq";
    public static final String CONTENT_GENERATED_ROUTING_KEY = "content.generated";
    public static final String IMAGE_GENERATED_ROUTING_KEY = "image.generated";
    public static final String CONTENT_PUBLISHED_ROUTING_KEY = "content.published";

    @Bean
    public DirectExchange publisherExchange() {
        return new DirectExchange(PUBLISHER_EXCHANGE, true, false);
    }

    @Bean
    public Queue contentGeneratedQueue() {
        return new Queue(CONTENT_GENERATED_QUEUE, true, false, false);
    }

    @Bean
    public Queue imageGeneratedQueue() {
        return new Queue(IMAGE_GENERATED_QUEUE, true, false, false);
    }

    @Bean
    public Queue publisherDeadLetterQueue() {
        return new Queue(PUBLISHER_DLQ, true, false, false);
    }

    @Bean
    public Binding contentGeneratedBinding() {
        return BindingBuilder.bind(contentGeneratedQueue())
            .to(new DirectExchange("atlasdeals.ai.exchange"))
            .with(CONTENT_GENERATED_ROUTING_KEY);
    }

    @Bean
    public Binding imageGeneratedBinding() {
        return BindingBuilder.bind(imageGeneratedQueue())
            .to(new DirectExchange("atlasdeals.image.exchange"))
            .with(IMAGE_GENERATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
