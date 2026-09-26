package com.atlasdeals.promotionengine.infrastructure.messaging;

import com.atlasdeals.common.domain.events.PriceUpdatedEvent;
import com.atlasdeals.common.domain.events.ProductCollectedEvent;
import com.atlasdeals.promotionengine.application.service.PromotionDetectionService;
import com.atlasdeals.promotionengine.domain.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RabbitListener(queues = "promotion.engine.q")
public class PromotionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PromotionEventConsumer.class);

    private final PromotionDetectionService detectionService;
    private final PromotionRepository promotionRepository;

    public PromotionEventConsumer(PromotionDetectionService detectionService,
                                  PromotionRepository promotionRepository) {
        this.detectionService = detectionService;
        this.promotionRepository = promotionRepository;
    }

    @RabbitHandler
    @Transactional
    public void handleProductCollected(ProductCollectedEvent event) {
        log.info("Received ProductCollectedEvent for product {}", event.getProductId().getValue());
        detectionService.analyzeProduct(event);
    }

    @RabbitHandler
    @Transactional
    public void handlePriceUpdated(PriceUpdatedEvent event) {
        log.info("Received PriceUpdatedEvent for product {}", event.getProductId().getValue());

        promotionRepository.findByProductId(event.getProductId().getValue())
            .ifPresentOrElse(promotion -> {
                log.info("Product {} has existing promotion (id={}), re-evaluation may be needed",
                    event.getProductId().getValue(), promotion.getId());
            }, () -> {
                log.info("Product {} has no existing promotion, price update ignored",
                    event.getProductId().getValue());
            });
    }
}
