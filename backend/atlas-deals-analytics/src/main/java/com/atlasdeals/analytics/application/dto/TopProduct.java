package com.atlasdeals.analytics.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopProduct(
    UUID productId,
    String title,
    long clicks,
    long conversions,
    BigDecimal revenue,
    double conversionRate
) {
}
