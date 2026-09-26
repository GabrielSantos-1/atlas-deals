package com.atlasdeals.collector.infrastructure.scheduler;

import com.atlasdeals.collector.application.dto.CollectionResult;
import com.atlasdeals.collector.application.port.ProductCollector;
import com.atlasdeals.collector.domain.model.CollectorStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CollectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(CollectionScheduler.class);

    private final List<ProductCollector> collectors;
    private final Map<String, CollectorStatus> statusMap;

    public CollectionScheduler(List<ProductCollector> collectors) {
        this.collectors = collectors;
        this.statusMap = new LinkedHashMap<>();
        for (ProductCollector c : collectors) {
            statusMap.put(c.getConfig().storeName(),
                c.getConfig().enabled() ? CollectorStatus.ACTIVE : CollectorStatus.DISABLED);
        }
    }

    @Scheduled(cron = "${collector.schedule:0 */15 * * * *}")
    public void runScheduledCollection() {
        log.info("Starting scheduled collection run");
        for (ProductCollector collector : collectors) {
            String name = collector.getConfig().storeName();
            CollectorStatus status = statusMap.getOrDefault(name, CollectorStatus.DISABLED);
            if (status != CollectorStatus.ACTIVE) {
                log.debug("Skipping {} - status is {}", name, status);
                continue;
            }
            try {
                CollectionResult result = collector.collect();
                log.info("Scheduled collection for {}: {} items ({} new, {} updated, {} errors)",
                    name, result.itemsCollected(), result.itemsNew(), result.itemsUpdated(), result.errors());
            } catch (Exception e) {
                log.error("Scheduled collection failed for {}: {}", name, e.getMessage(), e);
                statusMap.put(name, CollectorStatus.ERROR);
            }
        }
        log.info("Scheduled collection run completed");
    }

    public void updateStatus(String storeName, CollectorStatus status) {
        statusMap.put(storeName, status);
    }

    public CollectorStatus getStatus(String storeName) {
        return statusMap.getOrDefault(storeName, CollectorStatus.DISABLED);
    }

    public Map<String, CollectorStatus> getAllStatuses() {
        return new LinkedHashMap<>(statusMap);
    }
}
