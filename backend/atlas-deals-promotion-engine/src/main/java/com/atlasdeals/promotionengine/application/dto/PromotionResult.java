package com.atlasdeals.promotionengine.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PromotionResult(
    ProductData productData,
    BigDecimal score,
    List<String> rulesApplied,
    boolean qualified
) {
    public record ProductData(
        String productId,
        String title,
        BigDecimal currentPrice,
        BigDecimal originalPrice,
        BigDecimal discountPercentage,
        String category,
        String storeName,
        String storeId,
        String url,
        String imageUrl,
        boolean inStock,
        Map<String, Object> metadata
    ) {}
}
