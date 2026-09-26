package com.atlasdeals.analytics.application.dto;

import java.math.BigDecimal;

public record DashboardMetrics(
    BigDecimal totalRevenue,
    long totalClicks,
    long totalConversions,
    long totalViews,
    double averageCTR,
    double averageConversionRate,
    double revenueChange,
    double clickChange
) {
}
