package com.atlasdeals.common.application.dto;

import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.Promotion.PromotionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PromotionDTO(
    java.util.UUID id,
    ProductId productId,
    String title,
    Money currentPrice,
    Money originalPrice,
    BigDecimal discountPercentage,
    BigDecimal score,
    String category,
    String storeName,
    String url,
    String imageUrl,
    boolean inStock,
    List<String> tags,
    String reason,
    PromotionStatus status,
    Instant detectedAt,
    Instant publishedAt
) {}
