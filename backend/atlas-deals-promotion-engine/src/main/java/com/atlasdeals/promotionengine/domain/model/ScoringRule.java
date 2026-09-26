package com.atlasdeals.promotionengine.domain.model;

public enum ScoringRule {
    DISCOUNT_DEPTH(2.5),
    LOWEST_PRICE_30_DAYS(2.0),
    LOWEST_PRICE_HISTORICAL(1.5),
    COUPON_AVAILABLE(1.0),
    HIGH_CONVERSION(1.5),
    STORE_REPUTATION(1.0),
    STOCK_AVAILABLE(0.5),
    POPULAR_PRODUCT(1.5),
    SHIPPING_FREE(0.5);

    private final double weight;

    ScoringRule(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
