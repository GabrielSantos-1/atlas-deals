package com.atlasdeals.publisher.infrastructure.messaging;

import com.atlasdeals.common.application.port.IdempotencyService;
import com.atlasdeals.common.domain.events.ContentGeneratedEvent;
import com.atlasdeals.common.domain.events.ImageGeneratedEvent;
import com.atlasdeals.publisher.application.service.PublicationService;
import com.atlasdeals.publisher.infrastructure.config.PublisherConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublisherEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PublisherEventConsumer.class);

    static final String CONTENT_CONSUMER = "publisher.content";

    private final PublicationService publicationService;
    private final PublisherConfig publisherConfig;
    private final IdempotencyService idempotencyService;

    /**
     * Process-local cache correlating an image event with a later content event. It is kept as
     * a fallback only: the event payload is the durable source of truth, because this map is
     * lost on restart and is not shared between instances.
     */
    private final Map<String, String> pendingImageUrls = new ConcurrentHashMap<>();

    public PublisherEventConsumer(PublicationService publicationService,
                                  PublisherConfig publisherConfig,
                                  IdempotencyService idempotencyService) {
        this.publicationService = publicationService;
        this.publisherConfig = publisherConfig;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Runs in a single transaction: the idempotency marker and every publication created from
     * the event commit together. Any exception propagates to the listener container, which
     * rejects the message so the broker redelivers it; the redelivery is then filtered by the
     * marker.
     */
    @RabbitListener(queues = "${amqp.queue.content-generated:content.generated.q}")
    @Transactional
    public void handleContentGenerated(ContentGeneratedEvent event) {
        String eventId = event.getEventId().toString();
        log.info("Received ContentGeneratedEvent {} for product: {}", eventId, event.getProductId());

        if (!idempotencyService.tryMarkProcessed(eventId, CONTENT_CONSUMER)) {
            log.info("ContentGeneratedEvent {} already processed by {}, skipping",
                eventId, CONTENT_CONSUMER);
            return;
        }

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
            if (imageUrl == null) {
                imageUrl = event.getImageUrl();
            }

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
