package com.atlasdeals.aiengine.infrastructure.messaging;

import com.atlasdeals.aiengine.application.service.ContentGenerationService;
import com.atlasdeals.common.domain.events.PromotionDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AiEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AiEventConsumer.class);

    private final ContentGenerationService contentGenerationService;

    public AiEventConsumer(ContentGenerationService contentGenerationService) {
        this.contentGenerationService = contentGenerationService;
    }

    @RabbitListener(queues = "${amqp.queue.promotion-detected:ai.promotion.detected.q}")
    public void handlePromotionDetected(PromotionDetectedEvent event) {
        log.info("Received PromotionDetectedEvent for product: {}", event.getProductId());

        try {
            contentGenerationService.generateForPromotion(event);
            log.info("AI content generation completed for product: {}", event.getProductId());
        } catch (Exception e) {
            log.error("Failed to generate AI content for product: {}", event.getProductId(), e);
        }
    }
}
