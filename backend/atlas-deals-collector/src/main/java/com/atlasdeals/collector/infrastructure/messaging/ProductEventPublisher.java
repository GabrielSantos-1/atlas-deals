package com.atlasdeals.collector.infrastructure.messaging;

import com.atlasdeals.common.domain.events.ProductCollectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);
    private static final String EXCHANGE = "atlas-deals";
    private static final String ROUTING_KEY = "product.collected";

    private final RabbitTemplate rabbitTemplate;

    public ProductEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ProductCollectedEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
            log.info("Published ProductCollectedEvent for product {} from store {}", 
                event.getProductId(), event.getStoreName());
        } catch (Exception e) {
            log.error("Failed to publish ProductCollectedEvent for product {}: {}", 
                event.getProductId(), e.getMessage(), e);
        }
    }

    public void publishBatch(java.util.List<ProductCollectedEvent> events) {
        for (ProductCollectedEvent event : events) {
            publish(event);
        }
    }
}
