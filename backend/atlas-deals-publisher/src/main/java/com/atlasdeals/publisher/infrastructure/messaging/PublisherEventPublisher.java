package com.atlasdeals.publisher.infrastructure.messaging;

import com.atlasdeals.common.domain.events.ContentPublishedEvent;
import com.atlasdeals.publisher.domain.model.Publication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PublisherEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PublisherEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PublisherEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishContentPublished(Publication publication) {
        ContentPublishedEvent event = new ContentPublishedEvent(
            com.atlasdeals.common.domain.ProductId.of(UUID.fromString(publication.getProductId().toString())),
            publication.getPlatform(),
            publication.getPlatformPostId(),
            publication.getStatus().name(),
            publication.getContent(),
            publication.getMetadata(),
            publication.getPublishedAt() != null ? publication.getPublishedAt() : Instant.now()
        );

        log.info("Publishing ContentPublishedEvent for product {} on platform {}",
            publication.getProductId(), publication.getPlatform());

        rabbitTemplate.convertAndSend(
            "atlasdeals.publisher.exchange",
            "content.published",
            event
        );
    }
}
