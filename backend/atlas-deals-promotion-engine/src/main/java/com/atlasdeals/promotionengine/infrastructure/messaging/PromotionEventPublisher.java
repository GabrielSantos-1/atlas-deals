package com.atlasdeals.promotionengine.infrastructure.messaging;

import static com.atlasdeals.promotionengine.infrastructure.config.RabbitMQConfig.EXCHANGE;
import static com.atlasdeals.promotionengine.infrastructure.config.RabbitMQConfig.PROMOTION_DETECTED_ROUTING_KEY;

import com.atlasdeals.common.domain.events.PromotionDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PromotionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PromotionEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PromotionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPromotionDetected(PromotionDetectedEvent event) {
        log.info("Publishing PromotionDetectedEvent for product {} to exchange {} with routing key {}",
            event.getProductId().getValue(), EXCHANGE, PROMOTION_DETECTED_ROUTING_KEY);
        rabbitTemplate.convertAndSend(EXCHANGE, PROMOTION_DETECTED_ROUTING_KEY, event);
    }
}
