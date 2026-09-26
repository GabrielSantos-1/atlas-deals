package com.atlasdeals.analytics.application.dto;

import java.math.BigDecimal;
import java.util.Map;

public record RevenueBreakdown(
    Map<String, BigDecimal> byDimension
) {
}
