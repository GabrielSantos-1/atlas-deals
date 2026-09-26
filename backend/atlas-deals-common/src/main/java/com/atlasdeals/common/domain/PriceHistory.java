package com.atlasdeals.common.domain;

import java.math.BigDecimal;
import java.time.Instant;

public final class PriceHistory extends ValueObject {
    private final ProductId productId;
    private final Money price;
    private final Money cashPrice;
    private final Money installmentPrice;
    private final Integer installmentCount;
    private final String coupon;
    private final BigDecimal shippingCost;
    private final StoreId storeId;
    private final String storeName;
    private final String category;
    private final String brand;
    private final String model;
    private final boolean inStock;
    private final Instant recordedAt;

    private PriceHistory(Builder builder) {
        this.productId = builder.productId;
        this.price = builder.price;
        this.cashPrice = builder.cashPrice;
        this.installmentPrice = builder.installmentPrice;
        this.installmentCount = builder.installmentCount;
        this.coupon = builder.coupon;
        this.shippingCost = builder.shippingCost;
        this.storeId = builder.storeId;
        this.storeName = builder.storeName;
        this.category = builder.category;
        this.brand = builder.brand;
        this.model = builder.model;
        this.inStock = builder.inStock;
        this.recordedAt = builder.recordedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ProductId getProductId() { return productId; }
    public Money getPrice() { return price; }
    public Money getCashPrice() { return cashPrice; }
    public Money getInstallmentPrice() { return installmentPrice; }
    public Integer getInstallmentCount() { return installmentCount; }
    public String getCoupon() { return coupon; }
    public BigDecimal getShippingCost() { return shippingCost; }
    public StoreId getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public boolean isInStock() { return inStock; }
    public Instant getRecordedAt() { return recordedAt; }

    public static class Builder {
        private ProductId productId;
        private Money price;
        private Money cashPrice;
        private Money installmentPrice;
        private Integer installmentCount;
        private String coupon;
        private BigDecimal shippingCost;
        private StoreId storeId;
        private String storeName;
        private String category;
        private String brand;
        private String model;
        private boolean inStock;
        private Instant recordedAt;

        public Builder productId(ProductId productId) { this.productId = productId; return this; }
        public Builder price(Money price) { this.price = price; return this; }
        public Builder cashPrice(Money cashPrice) { this.cashPrice = cashPrice; return this; }
        public Builder installmentPrice(Money installmentPrice) { this.installmentPrice = installmentPrice; return this; }
        public Builder installmentCount(Integer installmentCount) { this.installmentCount = installmentCount; return this; }
        public Builder coupon(String coupon) { this.coupon = coupon; return this; }
        public Builder shippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; return this; }
        public Builder storeId(StoreId storeId) { this.storeId = storeId; return this; }
        public Builder storeName(String storeName) { this.storeName = storeName; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder brand(String brand) { this.brand = brand; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder inStock(boolean inStock) { this.inStock = inStock; return this; }
        public Builder recordedAt(Instant recordedAt) { this.recordedAt = recordedAt; return this; }
        public PriceHistory build() { return new PriceHistory(this); }
    }
}
