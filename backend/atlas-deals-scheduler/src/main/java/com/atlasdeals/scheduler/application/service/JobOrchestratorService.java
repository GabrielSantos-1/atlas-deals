package com.atlasdeals.scheduler.application.service;

import com.atlasdeals.scheduler.domain.model.JobStatus;
import com.atlasdeals.scheduler.domain.model.ScheduledJob;
import com.atlasdeals.scheduler.domain.model.ScheduledJob.JobType;
import com.atlasdeals.scheduler.domain.repository.ScheduledJobRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(JobOrchestratorService.class);

    private final ScheduledJobRepository jobRepository;
    private final DistributedLockService lockService;

    public JobOrchestratorService(ScheduledJobRepository jobRepository, DistributedLockService lockService) {
        this.jobRepository = jobRepository;
        this.lockService = lockService;
    }

    @Transactional
    public void executeJob(UUID jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!job.isEnabled()) {
            log.warn("Job {} is disabled, skipping", job.getName());
            return;
        }

        if (!lockService.acquireLock(job.getName())) {
            job.markSkipped();
            jobRepository.save(job);
            return;
        }

        try {
            job.markRunning();
            jobRepository.save(job);

            executeByType(job);

            job.markSuccess();
            log.info("Job {} executed successfully", job.getName());
        } catch (Exception e) {
            job.markFailed();
            log.error("Job {} failed: {}", job.getName(), e.getMessage(), e);
        } finally {
            job.setNextRunAt(calculateNextRun(job));
            jobRepository.save(job);
            lockService.releaseLock(job.getName());
        }
    }

    private void executeByType(ScheduledJob job) {
        JobType type = job.getType();
        switch (type) {
            case PRODUCT_COLLECTION -> triggerService("atlas-deals-collector", "/api/collector/run");
            case PROMOTION_SCAN -> triggerService("atlas-deals-promotion-engine", "/api/promotions/scan");
            case CONTENT_GENERATION -> triggerService("atlas-deals-ai-engine", "/api/ai/generate");
            case PUBLICATION -> triggerService("atlas-deals-publisher", "/api/publisher/publish");
            case METRICS_AGGREGATION -> triggerService("atlas-deals-analytics", "/api/analytics/aggregate");
            case REPORT -> triggerService("atlas-deals-analytics", "/api/analytics/reports/generate");
        }
    }

    private void triggerService(String serviceName, String path) {
        log.info("Triggering {} via {}", serviceName, path);
    }

    private Instant calculateNextRun(ScheduledJob job) {
        return Instant.now().plusSeconds(3600);
    }

    @Transactional
    public void toggleJob(UUID jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        job.toggle();
        jobRepository.save(job);
        log.info("Job {} toggled to {}", job.getName(), job.isEnabled());
    }

    @Transactional
    public void runNow(UUID jobId) {
        executeJob(jobId);
    }
}
