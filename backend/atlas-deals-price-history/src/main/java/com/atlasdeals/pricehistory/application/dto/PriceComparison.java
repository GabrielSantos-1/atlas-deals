package com.atlasdeals.pricehistory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PriceComparison(
    UUID productId1,
    UUID productId2,
    PriceStats stats1,
    PriceStats stats2,
    BigDecimal priceDifference,
    BigDecimal percentageDifference,
    String betterValueProductId
) {
}
