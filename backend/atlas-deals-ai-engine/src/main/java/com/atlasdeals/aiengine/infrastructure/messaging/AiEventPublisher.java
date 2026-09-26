package com.atlasdeals.aiengine.infrastructure.messaging;

import com.atlasdeals.aiengine.infrastructure.config.RabbitMQConfig;
import com.atlasdeals.common.domain.events.ContentGeneratedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AiEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AiEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public AiEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishContentGenerated(ContentGeneratedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AI_EXCHANGE,
                RabbitMQConfig.CONTENT_GENERATED_ROUTING_KEY,
                event
            );
            log.info("Published ContentGeneratedEvent for product: {}", event.getProductId());
        } catch (Exception e) {
            log.error("Failed to publish ContentGeneratedEvent for product: {}", event.getProductId(), e);
        }
    }
}
