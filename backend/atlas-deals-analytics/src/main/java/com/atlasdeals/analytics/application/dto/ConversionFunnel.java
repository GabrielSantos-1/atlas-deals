package com.atlasdeals.analytics.application.dto;

public record ConversionFunnel(
    long views,
    long clicks,
    long conversions,
    double viewsToClicksDropoff,
    double clicksToConversionsDropoff
) {
}
