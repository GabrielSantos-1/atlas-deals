package com.atlasdeals.pricehistory.infrastructure.messaging;

import com.atlasdeals.common.domain.events.ProductCollectedEvent;
import com.atlasdeals.pricehistory.application.service.PriceHistoryService;
import com.atlasdeals.pricehistory.domain.model.PriceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PriceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PriceEventConsumer.class);

    private final PriceHistoryService priceHistoryService;

    public PriceEventConsumer(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @RabbitListener(queues = "price.history.q")
    public void handleProductCollected(ProductCollectedEvent event) {
        log.info("Received ProductCollectedEvent for product {}", event.getProductId().getValue());

        PriceRecord record = PriceRecord.builder()
            .productId(event.getProductId().getValue())
            .storeId(event.getStoreId().getValue())
            .storeName(event.getStoreName())
            .price(event.getPrice().getAmount())
            .originalPrice(event.getOriginalPrice() != null ? event.getOriginalPrice().getAmount() : null)
            .inStock(event.isInStock())
            .category(event.getCategory())
            .brand(event.getBrand())
            .model(event.getModel())
            .externalId(event.getExternalId())
            .recordedAt(event.getCollectedAt())
            .available(true)
            .build();

        priceHistoryService.recordPrice(record);

        log.info("PriceRecord saved for product {}", event.getProductId().getValue());
    }
}
