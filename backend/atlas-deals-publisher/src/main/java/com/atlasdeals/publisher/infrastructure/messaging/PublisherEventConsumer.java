package com.atlasdeals.publisher.infrastructure.messaging;

import com.atlasdeals.common.domain.events.ContentGeneratedEvent;
import com.atlasdeals.common.domain.events.ImageGeneratedEvent;
import com.atlasdeals.publisher.application.service.PublicationService;
import com.atlasdeals.publisher.infrastructure.config.PublisherConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublisherEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PublisherEventConsumer.class);

    private final PublicationService publicationService;
    private final PublisherConfig publisherConfig;
    private final Map<String, String> pendingImageUrls = new ConcurrentHashMap<>();

    public PublisherEventConsumer(PublicationService publicationService, PublisherConfig publisherConfig) {
        this.publicationService = publicationService;
        this.publisherConfig = publisherConfig;
    }

    @RabbitListener(queues = "${amqp.queue.content-generated:content.generated.q}")
    public void handleContentGenerated(ContentGeneratedEvent event) {
        log.info("Received ContentGeneratedEvent for product: {}", event.getProductId());

        try {
            var contentByPlatform = event.getContentByPlatform();
            if (contentByPlatform == null || contentByPlatform.isEmpty()) {
                log.warn("No platform content found for product: {}", event.getProductId());
                return;
            }

            for (var entry : contentByPlatform.entrySet()) {
                String platform = entry.getKey().toUpperCase();
                if (!publisherConfig.isPlatformEnabled(platform)) {
                    log.debug("Platform {} is disabled, skipping publication for product {}",
                        platform, event.getProductId());
                    continue;
                }

                String content = entry.getValue();
                if (content == null || content.isBlank()) {
                    log.debug("Empty content for platform {} on product {}, skipping",
                        platform, event.getProductId());
                    continue;
                }

                String productIdStr = event.getProductId().getValue().toString();
                String key = productIdStr + ":" + platform;
                String imageUrl = pendingImageUrls.remove(key);

                publicationService.schedulePublication(
                    UUID.fromString(productIdStr),
                    platform,
                    content,
                    imageUrl,
                    null,
                    null
                );

                log.info("Created publication for product {} on platform {} with image {}",
                    event.getProductId(), platform, imageUrl != null ? "attached" : "none");
            }
        } catch (Exception e) {
            log.error("Failed to process ContentGeneratedEvent for product: {}",
                event.getProductId(), e);
        }
    }

    @RabbitListener(queues = "${amqp.queue.image-generated:image.generated.q}")
    public void handleImageGenerated(ImageGeneratedEvent event) {
        log.info("Received ImageGeneratedEvent for product {} on platform {}",
            event.getProductId(), event.getPlatform());

        try {
            String key = event.getProductId() + ":" + event.getPlatform().toUpperCase();
            pendingImageUrls.put(key, event.getLocalPath());

            log.debug("Stored image path for product {} on platform {}: {}",
                event.getProductId(), event.getPlatform(), event.getLocalPath());
        } catch (Exception e) {
            log.error("Failed to process ImageGeneratedEvent: {}", e.getMessage(), e);
        }
    }
}
