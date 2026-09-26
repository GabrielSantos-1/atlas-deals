package com.atlasdeals.imagegenerator.infrastructure.messaging;

import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.events.ContentGeneratedEvent;
import com.atlasdeals.imagegenerator.application.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ImageEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ImageEventConsumer.class);

    private final ImageService imageService;

    public ImageEventConsumer(ImageService imageService) {
        this.imageService = imageService;
    }

    @RabbitListener(queues = "${amqp.queue.content-generated:content.generated.q}")
    public void handleContentGenerated(ContentGeneratedEvent event) {
        log.info("Received ContentGeneratedEvent for product: {}", event.getProductId());

        try {
            ProductId productId = event.getProductId();
            String title = event.getTitle();
            String imageUrl = event.getImageUrl();
            String storeName = event.getStoreName() != null ? event.getStoreName() : "default";
            BigDecimal price = event.getPrice();
            BigDecimal originalPrice = event.getOriginalPrice();

            if (imageUrl == null) {
                log.warn("No image URL in ContentGeneratedEvent for product {}, skipping image generation", productId);
                return;
            }

            imageService.downloadProductImage(productId, imageUrl, storeName)
                .thenAccept(productImage -> {
                    if (productImage.getErrorMessage() == null) {
                        imageService.generatePromotionalImages(
                            productId, title, storeName, price, originalPrice);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Failed to process images for product {}: {}",
                        productId, ex.getMessage(), ex);
                    return null;
                });

        } catch (Exception e) {
            log.error("Failed to handle ContentGeneratedEvent: {}", e.getMessage(), e);
        }
    }
}
