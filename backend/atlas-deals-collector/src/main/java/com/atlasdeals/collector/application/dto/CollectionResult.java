package com.atlasdeals.collector.application.dto;

import java.time.Duration;
import java.time.Instant;

public record CollectionResult(
    String storeName,
    int itemsCollected,
    int itemsNew,
    int itemsUpdated,
    int errors,
    Duration duration,
    Instant collectedAt
) {
    public static CollectionResult success(String storeName, int itemsNew, int itemsUpdated, Duration duration) {
        return new CollectionResult(storeName, itemsNew + itemsUpdated, itemsNew, itemsUpdated, 0, duration, Instant.now());
    }

    public static CollectionResult failure(String storeName, int errors, Duration duration) {
        return new CollectionResult(storeName, 0, 0, 0, errors, duration, Instant.now());
    }
}
