package com.atlasdeals.pricehistory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PriceStats(
    UUID productId,
    BigDecimal currentPrice,
    BigDecimal lowestPrice,
    BigDecimal highestPrice,
    BigDecimal averagePrice,
    BigDecimal percentageChange,
    Trend trend,
    int dataPoints
) {
    public enum Trend {
        UP, DOWN, STABLE
    }
}
