package com.atlasdeals.collector.application.dto;

public record ConnectorConfig(
    String storeName,
    String baseUrl,
    String apiKey,
    int maxRequestsPerMinute,
    boolean enabled
) {
}
