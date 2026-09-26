package com.atlasdeals.scheduler.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scheduled_jobs")
public class ScheduledJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private JobType type;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_run_status", length = 20)
    private JobStatus lastRunStatus;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ScheduledJob() {
    }

    public ScheduledJob(String name, JobType type, String cronExpression, boolean enabled) {
        this.name = name;
        this.type = type;
        this.cronExpression = cronExpression;
        this.enabled = enabled;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public void markRunning() {
        this.lastRunStatus = JobStatus.RUNNING;
        this.lastRunAt = Instant.now();
    }

    public void markSuccess() {
        this.lastRunStatus = JobStatus.SUCCESS;
    }

    public void markFailed() {
        this.lastRunStatus = JobStatus.FAILED;
    }

    public void markSkipped() {
        this.lastRunStatus = JobStatus.SKIPPED;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public JobType getType() { return type; }
    public void setType(JobType type) { this.type = type; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Instant lastRunAt) { this.lastRunAt = lastRunAt; }
    public JobStatus getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(JobStatus lastRunStatus) { this.lastRunStatus = lastRunStatus; }
    public Instant getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(Instant nextRunAt) { this.nextRunAt = nextRunAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public enum JobType {
        PRODUCT_COLLECTION,
        PROMOTION_SCAN,
        CONTENT_GENERATION,
        PUBLICATION,
        METRICS_AGGREGATION,
        REPORT
    }
}
