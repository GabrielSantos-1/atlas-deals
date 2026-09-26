package com.atlasdeals.imagegenerator.infrastructure.messaging;

import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.events.ImageGeneratedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ImageEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ImageEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public ImageEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishImageGenerated(ProductId productId, String platform, String localPath,
                                       int width, int height, String format) {
        ImageGeneratedEvent event = new ImageGeneratedEvent(
            productId, platform, localPath, width, height, format);

        log.info("Publishing ImageGeneratedEvent for product {} on platform {}",
            productId, platform);

        rabbitTemplate.convertAndSend(
            "atlasdeals.image.exchange",
            "image.generated",
            event
        );
    }
}
