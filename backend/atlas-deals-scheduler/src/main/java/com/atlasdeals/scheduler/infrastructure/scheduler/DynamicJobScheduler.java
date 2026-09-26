package com.atlasdeals.scheduler.infrastructure.scheduler;

import com.atlasdeals.scheduler.application.service.JobOrchestratorService;
import com.atlasdeals.scheduler.domain.model.ScheduledJob;
import com.atlasdeals.scheduler.domain.repository.ScheduledJobRepository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
public class DynamicJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(DynamicJobScheduler.class);

    private final ScheduledJobRepository jobRepository;
    private final TaskScheduler taskScheduler;
    private final JobOrchestratorService orchestratorService;
    private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DynamicJobScheduler(ScheduledJobRepository jobRepository,
                                TaskScheduler taskScheduler,
                                JobOrchestratorService orchestratorService) {
        this.jobRepository = jobRepository;
        this.taskScheduler = taskScheduler;
        this.orchestratorService = orchestratorService;
    }

    @PostConstruct
    public void initializeJobs() {
        var jobs = jobRepository.findByEnabledTrue();
        for (ScheduledJob job : jobs) {
            registerJob(job);
        }
        log.info("Initialized {} scheduled jobs", jobs.size());
    }

    public void registerJob(ScheduledJob job) {
        cancelJob(job.getId());
        if (!job.isEnabled()) {
            return;
        }
        ScheduledFuture<?> future = taskScheduler.schedule(
            () -> orchestratorService.executeJob(job.getId()),
            new CronTrigger(job.getCronExpression())
        );
        scheduledTasks.put(job.getId(), future);
        log.info("Registered job: {} with cron: {}", job.getName(), job.getCronExpression());
    }

    public void cancelJob(UUID jobId) {
        ScheduledFuture<?> future = scheduledTasks.remove(jobId);
        if (future != null) {
            future.cancel(false);
        }
    }

    public boolean isJobScheduled(UUID jobId) {
        ScheduledFuture<?> future = scheduledTasks.get(jobId);
        return future != null && !future.isCancelled();
    }
}
