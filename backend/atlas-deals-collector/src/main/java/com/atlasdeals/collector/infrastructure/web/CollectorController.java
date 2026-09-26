package com.atlasdeals.collector.infrastructure.web;

import com.atlasdeals.collector.application.dto.CollectionResult;
import com.atlasdeals.collector.application.port.ProductCollector;
import com.atlasdeals.collector.domain.model.CollectorStatus;
import com.atlasdeals.collector.infrastructure.scheduler.CollectionScheduler;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collector")
public class CollectorController {

    private final List<ProductCollector> collectors;
    private final CollectionScheduler scheduler;

    public CollectorController(List<ProductCollector> collectors, CollectionScheduler scheduler) {
        this.collectors = collectors;
        this.scheduler = scheduler;
    }

    @PostMapping("/collect")
    public ResponseEntity<CollectionResult> triggerCollect(@RequestParam(defaultValue = "") String store) {
        for (ProductCollector collector : collectors) {
            if (collector.supports(store)) {
                CollectorStatus status = scheduler.getStatus(store);
                if (status == CollectorStatus.DISABLED || status == CollectorStatus.PAUSED) {
                    return ResponseEntity.ok(CollectionResult.failure(store, 1, java.time.Duration.ZERO));
                }
                CollectionResult result = collector.collect();
                if (result.errors() > 0) {
                    scheduler.updateStatus(store, CollectorStatus.ERROR);
                }
                return ResponseEntity.ok(result);
            }
        }
        return ResponseEntity.badRequest().body(CollectionResult.failure(store, 1, java.time.Duration.ZERO));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllStatuses() {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, CollectorStatus> statuses = scheduler.getAllStatuses();
        for (ProductCollector collector : collectors) {
            String name = collector.getConfig().storeName();
            result.put(name, Map.of(
                "status", statuses.getOrDefault(name, CollectorStatus.DISABLED),
                "config", collector.getConfig()
            ));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{store}/enable")
    public ResponseEntity<Void> enable(@PathVariable String store) {
        if (scheduler.getStatus(store) != null) {
            scheduler.updateStatus(store, CollectorStatus.ACTIVE);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{store}/disable")
    public ResponseEntity<Void> disable(@PathVariable String store) {
        if (scheduler.getStatus(store) != null) {
            scheduler.updateStatus(store, CollectorStatus.DISABLED);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
