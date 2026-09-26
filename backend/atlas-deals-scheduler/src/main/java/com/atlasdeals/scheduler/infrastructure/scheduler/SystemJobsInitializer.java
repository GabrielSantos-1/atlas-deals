package com.atlasdeals.scheduler.infrastructure.scheduler;

import com.atlasdeals.scheduler.domain.model.ScheduledJob;
import com.atlasdeals.scheduler.domain.model.ScheduledJob.JobType;
import com.atlasdeals.scheduler.domain.repository.ScheduledJobRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemJobsInitializer {

    private static final Logger log = LoggerFactory.getLogger(SystemJobsInitializer.class);

    private final ScheduledJobRepository jobRepository;
    private final DynamicJobScheduler dynamicJobScheduler;

    public SystemJobsInitializer(ScheduledJobRepository jobRepository, DynamicJobScheduler dynamicJobScheduler) {
        this.jobRepository = jobRepository;
        this.dynamicJobScheduler = dynamicJobScheduler;
    }

    @PostConstruct
    @Transactional
    public void createDefaultJobs() {
        List<ScheduledJob> existing = jobRepository.findByEnabledTrue();
        if (!existing.isEmpty()) {
            log.info("System jobs already initialized ({} active)", existing.size());
            return;
        }

        List<ScheduledJob> defaults = List.of(
            new ScheduledJob("Product Collection", JobType.PRODUCT_COLLECTION, "0 */15 * * * *", true),
            new ScheduledJob("Promotion Scan", JobType.PROMOTION_SCAN, "0 0 */2 * * *", true),
            new ScheduledJob("Content Generation", JobType.CONTENT_GENERATION, "0 30 */3 * * *", true),
            new ScheduledJob("Publication", JobType.PUBLICATION, "0 0 */4 * * *", true),
            new ScheduledJob("Metrics Aggregation", JobType.METRICS_AGGREGATION, "0 0 */1 * * *", true),
            new ScheduledJob("Daily Report", JobType.REPORT, "0 0 7 * * *", true)
        );

        for (ScheduledJob job : defaults) {
            if (jobRepository.findByName(job.getName()).isEmpty()) {
                ScheduledJob saved = jobRepository.save(job);
                dynamicJobScheduler.registerJob(saved);
                log.info("Created system job: {}", job.getName());
            }
        }
    }
}
