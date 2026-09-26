package com.atlasdeals.common.application.dto;

import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.StoreId;
import java.math.BigDecimal;
import java.time.Instant;

public record PriceHistoryDTO(
    ProductId productId,
    Money price,
    Money cashPrice,
    Money installmentPrice,
    Integer installmentCount,
    String coupon,
    BigDecimal shippingCost,
    StoreId storeId,
    String storeName,
    String category,
    String brand,
    String model,
    boolean inStock,
    Instant recordedAt
) {}
