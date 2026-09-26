package com.atlasdeals.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class PromotionScore extends ValueObject {
    private final BigDecimal discountScore;
    private final BigDecimal popularityScore;
    private final BigDecimal stockScore;
    private final BigDecimal categoryScore;
    private final BigDecimal brandScore;
    private final BigDecimal total;

    private PromotionScore(BigDecimal discountScore, BigDecimal popularityScore, BigDecimal stockScore,
                           BigDecimal categoryScore, BigDecimal brandScore, BigDecimal total) {
        this.discountScore = discountScore;
        this.popularityScore = popularityScore;
        this.stockScore = stockScore;
        this.categoryScore = categoryScore;
        this.brandScore = brandScore;
        this.total = total;
    }

    public static PromotionScore calculate(BigDecimal discountPercentage, int totalClicks,
                                           int totalConversions, boolean inStock, boolean isTopCategory,
                                           boolean isPremiumBrand) {
        BigDecimal discount = discountPercentage.multiply(BigDecimal.valueOf(2.5));

        double convRate = totalClicks > 0 ? (double) totalConversions / totalClicks : 0.0;
        BigDecimal popularity = BigDecimal.valueOf(convRate * 100).multiply(BigDecimal.valueOf(1.5));

        BigDecimal stock = inStock ? BigDecimal.valueOf(20) : BigDecimal.ZERO;

        BigDecimal category = isTopCategory ? BigDecimal.valueOf(15) : BigDecimal.valueOf(5);

        BigDecimal brand = isPremiumBrand ? BigDecimal.valueOf(10) : BigDecimal.valueOf(5);

        BigDecimal total = discount.add(popularity).add(stock).add(category).add(brand);
        total = total.setScale(2, RoundingMode.HALF_EVEN);

        return new PromotionScore(discount, popularity, stock, category, brand, total);
    }

    public static PromotionScore of(BigDecimal total) {
        return new PromotionScore(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                  BigDecimal.ZERO, BigDecimal.ZERO, total);
    }

    public boolean isHighValue() {
        return total.compareTo(BigDecimal.valueOf(70)) >= 0;
    }

    public boolean isMediumValue() {
        return total.compareTo(BigDecimal.valueOf(40)) >= 0 && total.compareTo(BigDecimal.valueOf(70)) < 0;
    }

    public boolean isLowValue() {
        return total.compareTo(BigDecimal.valueOf(40)) < 0;
    }

    public String getLevel() {
        if (isHighValue()) return "HIGH";
        if (isMediumValue()) return "MEDIUM";
        return "LOW";
    }

    public BigDecimal getDiscountScore() { return discountScore; }
    public BigDecimal getPopularityScore() { return popularityScore; }
    public BigDecimal getStockScore() { return stockScore; }
    public BigDecimal getCategoryScore() { return categoryScore; }
    public BigDecimal getBrandScore() { return brandScore; }
    public BigDecimal getTotal() { return total; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionScore that = (PromotionScore) o;
        return total.compareTo(that.total) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(total.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "PromotionScore{total=" + total + ", level=" + getLevel() + "}";
    }
}
