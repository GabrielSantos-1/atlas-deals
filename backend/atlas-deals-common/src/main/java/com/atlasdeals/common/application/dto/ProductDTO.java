package com.atlasdeals.common.application.dto;

import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.StoreId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record ProductDTO(
    ProductId id,
    StoreId storeId,
    String storeName,
    String externalId,
    String title,
    String description,
    Money currentPrice,
    Money originalPrice,
    String currency,
    String url,
    String imageUrl,
    String category,
    String brand,
    String model,
    boolean inStock,
    String sku,
    String ean,
    Money lowestPrice,
    Instant lowestPriceRecordedAt,
    Instant firstSeenAt,
    Instant lastUpdatedAt,
    Set<String> tags,
    int totalClicks,
    int totalConversions,
    Money totalRevenue,
    Money previousPrice,
    double conversionRate
) {
    public boolean isOnSale() {
        if (originalPrice == null || originalPrice.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return currentPrice.isLessThan(originalPrice);
    }

    public BigDecimal getDiscountPercentage() {
        if (originalPrice == null || originalPrice.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentPrice.discountPercentage(originalPrice);
    }
}
