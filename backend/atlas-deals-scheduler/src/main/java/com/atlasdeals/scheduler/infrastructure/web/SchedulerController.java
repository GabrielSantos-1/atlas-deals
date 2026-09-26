package com.atlasdeals.scheduler.infrastructure.web;

import com.atlasdeals.scheduler.application.service.JobOrchestratorService;
import com.atlasdeals.scheduler.domain.model.ScheduledJob;
import com.atlasdeals.scheduler.domain.repository.ScheduledJobRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private static final Logger log = LoggerFactory.getLogger(SchedulerController.class);

    private final JobOrchestratorService orchestratorService;
    private final ScheduledJobRepository jobRepository;

    public SchedulerController(JobOrchestratorService orchestratorService, ScheduledJobRepository jobRepository) {
        this.orchestratorService = orchestratorService;
        this.jobRepository = jobRepository;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<ScheduledJob>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }

    @PatchMapping("/jobs/{id}/toggle")
    public ResponseEntity<Void> toggleJob(@PathVariable UUID id) {
        orchestratorService.toggleJob(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/jobs/{id}/run-now")
    public ResponseEntity<Void> runNow(@PathVariable UUID id) {
        orchestratorService.runNow(id);
        return ResponseEntity.ok().build();
    }
}
