package com.atlasdeals.publisher.infrastructure.scheduler;

import com.atlasdeals.publisher.application.service.PublicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PublicationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PublicationScheduler.class);

    private final PublicationService publicationService;

    public PublicationScheduler(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Scheduled(fixedRateString = "${publisher.queue-poll-interval-ms:30000}")
    public void processQueuedPublications() {
        log.debug("Processing queued publications...");
        try {
            publicationService.processQueue();
        } catch (Exception e) {
            log.error("Error processing queued publications: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRateString = "${publisher.schedule-poll-interval-ms:60000}")
    public void processScheduledPublications() {
        log.debug("Checking scheduled publications...");
        try {
            publicationService.processScheduled();
        } catch (Exception e) {
            log.error("Error processing scheduled publications: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRateString = "${publisher.retry-interval-ms:300000}")
    public void retryFailedPublications() {
        log.debug("Checking failed publications for retry...");
        try {
            publicationService.retryFailed();
        } catch (Exception e) {
            log.error("Error retrying failed publications: {}", e.getMessage(), e);
        }
    }
}
