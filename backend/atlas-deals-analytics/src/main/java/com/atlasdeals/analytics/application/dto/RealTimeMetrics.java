package com.atlasdeals.analytics.application.dto;

import java.math.BigDecimal;

public record RealTimeMetrics(
    long last24hClicks,
    long last24hConversions,
    BigDecimal last24hRevenue,
    long activeProducts
) {
}
