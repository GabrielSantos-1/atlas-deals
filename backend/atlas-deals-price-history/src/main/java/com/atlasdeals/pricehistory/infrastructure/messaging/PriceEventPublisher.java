package com.atlasdeals.pricehistory.infrastructure.messaging;

import com.atlasdeals.common.domain.events.PriceUpdatedEvent;
import com.atlasdeals.pricehistory.application.service.PriceHistoryService.PriceUpdateNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PriceEventPublisher implements PriceUpdateNotifier {

    private static final Logger log = LoggerFactory.getLogger(PriceEventPublisher.class);
    private static final String EXCHANGE = "atlas-deals";
    private static final String ROUTING_KEY = "price.updated";

    private final RabbitTemplate rabbitTemplate;

    public PriceEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void notifyPriceUpdated(PriceUpdatedEvent event) {
        log.info("Publishing PriceUpdatedEvent for product {}", event.getProductId().getValue());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}
