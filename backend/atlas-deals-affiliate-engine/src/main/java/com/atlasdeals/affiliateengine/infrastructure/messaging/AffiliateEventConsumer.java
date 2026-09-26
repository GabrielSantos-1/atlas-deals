package com.atlasdeals.affiliateengine.infrastructure.messaging;

import com.atlasdeals.affiliateengine.application.service.AffiliateService;
import com.atlasdeals.common.domain.events.PromotionDetectedEvent;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RabbitListener(queues = "promotion.detected.q")
public class AffiliateEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AffiliateEventConsumer.class);

    private final AffiliateService affiliateService;

    public AffiliateEventConsumer(AffiliateService affiliateService) {
        this.affiliateService = affiliateService;
    }

    @RabbitHandler
    @Transactional
    public void handlePromotionDetected(PromotionDetectedEvent event) {
        UUID productId = event.getProductId().getValue();
        String storeName = event.getStoreName();
        String url = event.getUrl();

        log.info("Received PromotionDetectedEvent for product {} at store {}, auto-generating affiliate link",
            productId, storeName);

        try {
            affiliateService.generateAffiliateLink(productId, storeName, url, null);
            log.info("Auto-generated affiliate link for product {} on {}", productId, storeName);
        } catch (Exception e) {
            log.error("Failed to auto-generate affiliate link for product {} on {}: {}",
                productId, storeName, e.getMessage(), e);
        }
    }
}
