package com.atlasdeals.common.domain.specification;

import com.atlasdeals.common.domain.Product;
import com.atlasdeals.common.domain.Promotion;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public final class PromotionSpecification {

    private PromotionSpecification() {}

    public static Specification<Product> minDiscount(BigDecimal minPercentage) {
        return product -> product.isPromotion(minPercentage);
    }

    public static Specification<Product> minScore(BigDecimal minScore) {
        return product -> product.getCurrentPrice().discountPercentage(product.getOriginalPrice())
            .compareTo(minScore) >= 0;
    }

    public static Specification<Product> inStock() {
        return Product::isInStock;
    }

    public static Specification<Product> byCategory(String category) {
        return product -> product.getCategory() != null && product.getCategory().equalsIgnoreCase(category);
    }

    public static Specification<Product> byBrand(String brand) {
        return product -> product.getBrand() != null && product.getBrand().equalsIgnoreCase(brand);
    }

    public static Specification<Product> byStore(String storeName) {
        return product -> product.getStoreName() != null && product.getStoreName().equalsIgnoreCase(storeName);
    }

    public static Specification<Product> recentlyUpdated(Duration maxAge) {
        return product -> {
            Instant cutoff = Instant.now().minus(maxAge);
            return product.getLastUpdatedAt() != null && product.getLastUpdatedAt().isAfter(cutoff);
        };
    }

    public static Specification<Product> minPrice(BigDecimal minAmount, String currencyCode) {
        return product -> product.getCurrentPrice().getAmount().compareTo(minAmount) >= 0;
    }

    public static Specification<Product> maxPrice(BigDecimal maxAmount, String currencyCode) {
        return product -> product.getCurrentPrice().getAmount().compareTo(maxAmount) <= 0;
    }

    public static Specification<Promotion> byStatus(Promotion.PromotionStatus status) {
        return promotion -> promotion.getStatus() == status;
    }

    public static Specification<Promotion> pendingPublish() {
        return promotion -> promotion.getStatus() == Promotion.PromotionStatus.DETECTED
            || promotion.getStatus() == Promotion.PromotionStatus.QUEUED;
    }
}
