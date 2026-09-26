package com.atlasdeals.publisher.application.service;

import com.atlasdeals.publisher.application.dto.PublishRequest;
import com.atlasdeals.publisher.application.dto.PublishResult;
import com.atlasdeals.publisher.application.port.Publisher;
import com.atlasdeals.publisher.domain.model.Publication;
import com.atlasdeals.publisher.domain.model.PublicationStatus;
import com.atlasdeals.publisher.infrastructure.config.PublisherConfig;
import com.atlasdeals.publisher.infrastructure.messaging.PublisherEventPublisher;
import com.atlasdeals.publisher.infrastructure.persistence.PublicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PublicationService {

    private static final Logger log = LoggerFactory.getLogger(PublicationService.class);

    private final PublicationRepository publicationRepository;
    private final Map<String, Publisher> publisherMap;
    private final PublisherConfig publisherConfig;
    private final PublisherEventPublisher eventPublisher;

    public PublicationService(PublicationRepository publicationRepository,
                              Map<String, Publisher> publisherMap,
                              PublisherConfig publisherConfig,
                              PublisherEventPublisher eventPublisher) {
        this.publicationRepository = publicationRepository;
        this.publisherMap = publisherMap;
        this.publisherConfig = publisherConfig;
        this.eventPublisher = eventPublisher;
    }

    public Publication schedulePublication(UUID productId, String platform, String content,
                                           String imageUrl, String affiliateUrl, Instant scheduledFor) {
        Publication publication = new Publication(productId, platform, content,
            scheduledFor != null ? PublicationStatus.SCHEDULED : PublicationStatus.QUEUED);
        publication.setScheduledFor(scheduledFor);
        publication.setMaxRetries(publisherConfig.getMaxRetries());

        Map<String, Object> metadata = new java.util.LinkedHashMap<>();
        if (imageUrl != null) metadata.put("imageUrl", imageUrl);
        if (affiliateUrl != null) metadata.put("affiliateUrl", affiliateUrl);
        if (!metadata.isEmpty()) {
            publication.setMetadata(metadata);
        }

        return publicationRepository.save(publication);
    }

    @Transactional
    public PublishResult publishNow(UUID publicationId) {
        Publication publication = publicationRepository.findById(publicationId)
            .orElseThrow(() -> new IllegalArgumentException("Publication not found: " + publicationId));

        return executePublish(publication);
    }

    @Transactional
    public void processQueue() {
        List<Publication> queued = publicationRepository.findByStatusIn(
            List.of(PublicationStatus.QUEUED));

        for (Publication publication : queued) {
            try {
                executePublish(publication);
            } catch (Exception e) {
                log.error("Failed to process queued publication {}: {}", publication.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void processScheduled() {
        List<Publication> scheduled = publicationRepository.findByStatusAndScheduledForBefore(
            PublicationStatus.SCHEDULED, Instant.now());

        for (Publication publication : scheduled) {
            publication.setStatus(PublicationStatus.QUEUED);
            publicationRepository.save(publication);
        }

        if (!scheduled.isEmpty()) {
            log.info("Moved {} scheduled publications to QUEUED", scheduled.size());
        }
    }

    @Transactional
    public void retryFailed() {
        List<Publication> failed = publicationRepository.findByStatusAndRetryCountLessThanMax(
            PublicationStatus.FAILED);

        for (Publication publication : failed) {
            publication.setStatus(PublicationStatus.QUEUED);
            publicationRepository.save(publication);
        }

        if (!failed.isEmpty()) {
            log.info("Queued {} failed publications for retry", failed.size());
        }
    }

    private PublishResult executePublish(Publication publication) {
        Publisher publisher = publisherMap.get(publication.getPlatform().toLowerCase());
        if (publisher == null) {
            log.warn("No publisher found for platform: {}", publication.getPlatform());
            publication.setStatus(PublicationStatus.FAILED);
            publication.setErrorMessage("No publisher configured for platform: " + publication.getPlatform());
            publicationRepository.save(publication);
            return new PublishResult(false, null, publication.getErrorMessage(), null);
        }

        if (!publisher.isAvailable()) {
            log.warn("Publisher {} is not available", publication.getPlatform());
            return new PublishResult(false, null, "Publisher not available", null);
        }

        publication.setStatus(PublicationStatus.PROCESSING);
        publicationRepository.save(publication);

        try {
            PublishRequest request = new PublishRequest(
                publication.getProductId().toString(),
                publication.getContent(),
                publication.getMetadata() != null ? (String) publication.getMetadata().get("imageUrl") : null,
                publication.getMetadata() != null ? (String) publication.getMetadata().get("affiliateUrl") : null,
                publication.getMetadata()
            );

            PublishResult result = publisher.publish(request);

            if (result.success()) {
                publication.setStatus(PublicationStatus.PUBLISHED);
                publication.setPlatformPostId(result.platformPostId());
                publication.setPublishedAt(result.publishedAt() != null ? result.publishedAt() : Instant.now());
                publication.setErrorMessage(null);
                publication.setRetryCount(0);
                log.info("Successfully published to {} for product {} (postId: {})",
                    publication.getPlatform(), publication.getProductId(), result.platformPostId());

                eventPublisher.publishContentPublished(publication);
            } else {
                publication.setRetryCount(publication.getRetryCount() + 1);
                if (publication.getRetryCount() >= publication.getMaxRetries()) {
                    publication.setStatus(PublicationStatus.FAILED);
                }
                publication.setErrorMessage(result.errorMessage());
                log.warn("Failed to publish to {} for product {}: {}",
                    publication.getPlatform(), publication.getProductId(), result.errorMessage());
            }
        } catch (Exception e) {
            publication.setRetryCount(publication.getRetryCount() + 1);
            if (publication.getRetryCount() >= publication.getMaxRetries()) {
                publication.setStatus(PublicationStatus.FAILED);
            }
            publication.setErrorMessage(e.getMessage());
            log.error("Error publishing to {} for product {}: {}",
                publication.getPlatform(), publication.getProductId(), e.getMessage(), e);
        }

        return publicationRepository.save(publication).getStatus() == PublicationStatus.PUBLISHED
            ? new PublishResult(true, publication.getPlatformPostId(), null, publication.getPublishedAt())
            : new PublishResult(false, null, publication.getErrorMessage(), null);
    }

    @Transactional(readOnly = true)
    public Optional<Publication> getPublicationStatus(UUID publicationId) {
        return publicationRepository.findById(publicationId);
    }

    @Transactional(readOnly = true)
    public List<Publication> getPublicationsByProduct(UUID productId) {
        return publicationRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPlatformMetrics(String platform) {
        long totalPublished = publicationRepository.countByPlatformAndStatus(platform, PublicationStatus.PUBLISHED);
        long totalFailed = publicationRepository.countByPlatformAndStatus(platform, PublicationStatus.FAILED);
        long totalQueued = publicationRepository.countByPlatformAndStatus(platform, PublicationStatus.QUEUED);

        Instant lastHour = Instant.now().minus(Duration.ofHours(1));
        long publishedLastHour = publicationRepository.countByPlatformAndStatusSince(
            platform, PublicationStatus.PUBLISHED, lastHour);

        double successRate = (totalPublished + totalFailed) > 0
            ? (double) totalPublished / (totalPublished + totalFailed) * 100.0
            : 0.0;

        Double avgTimeSeconds = publicationRepository.avgPublishTimeSeconds(platform, PublicationStatus.PUBLISHED.name());

        return Map.of(
            "platform", platform,
            "totalPublished", totalPublished,
            "totalFailed", totalFailed,
            "totalQueued", totalQueued,
            "publishedLastHour", publishedLastHour,
            "successRate", Math.round(successRate * 100.0) / 100.0,
            "avgPublishTimeSeconds", avgTimeSeconds != null ? Math.round(avgTimeSeconds * 100.0) / 100.0 : null,
            "available", publisherMap.containsKey(platform.toLowerCase())
        );
    }

    @Transactional(readOnly = true)
    public List<String> getAvailablePlatforms() {
        return publisherMap.values().stream()
            .filter(Publisher::isAvailable)
            .map(Publisher::getPlatform)
            .collect(Collectors.toList());
    }
}
