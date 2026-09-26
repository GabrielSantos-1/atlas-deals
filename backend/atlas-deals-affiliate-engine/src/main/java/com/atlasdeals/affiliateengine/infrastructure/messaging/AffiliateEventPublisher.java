package com.atlasdeals.affiliateengine.infrastructure.messaging;

import static com.atlasdeals.affiliateengine.infrastructure.config.RabbitMQConfig.AFFILIATE_LINK_GENERATED_ROUTING_KEY;
import static com.atlasdeals.affiliateengine.infrastructure.config.RabbitMQConfig.EXCHANGE;

import com.atlasdeals.common.domain.events.AffiliateLinkGeneratedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AffiliateEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AffiliateEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public AffiliateEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAffiliateLinkGenerated(AffiliateLinkGeneratedEvent event) {
        log.info("Publishing AffiliateLinkGeneratedEvent for product {} to exchange {} with routing key {}",
            event.getProductId().getValue(), EXCHANGE, AFFILIATE_LINK_GENERATED_ROUTING_KEY);
        rabbitTemplate.convertAndSend(EXCHANGE, AFFILIATE_LINK_GENERATED_ROUTING_KEY, event);
    }
}
