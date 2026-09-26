package com.atlasdeals.pricehistory.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "price_records", indexes = {
    @Index(name = "idx_price_records_product_recorded", columnList = "productId, recordedAt"),
    @Index(name = "idx_price_records_store_product", columnList = "storeId, productId")
})
public class PriceRecord {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal cashPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal installmentPrice;

    @Column
    private int installmentCount;

    @Column
    private String coupon;

    @Column(precision = 12, scale = 2)
    private BigDecimal couponDiscount;

    @Column(precision = 12, scale = 2)
    private BigDecimal shippingCost;

    @Column(nullable = false)
    private boolean inStock;

    @Column(nullable = false)
    private boolean available;

    @Column
    private String category;

    @Column
    private String brand;

    @Column
    private String model;

    @Column
    private String externalId;

    @Column(nullable = false)
    private Instant recordedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public PriceRecord() {
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private PriceRecord(Builder builder) {
        this.id = builder.id;
        this.productId = builder.productId;
        this.storeId = builder.storeId;
        this.storeName = builder.storeName;
        this.price = builder.price;
        this.originalPrice = builder.originalPrice;
        this.cashPrice = builder.cashPrice;
        this.installmentPrice = builder.installmentPrice;
        this.installmentCount = builder.installmentCount;
        this.coupon = builder.coupon;
        this.couponDiscount = builder.couponDiscount;
        this.shippingCost = builder.shippingCost;
        this.inStock = builder.inStock;
        this.available = builder.available;
        this.category = builder.category;
        this.brand = builder.brand;
        this.model = builder.model;
        this.externalId = builder.externalId;
        this.recordedAt = builder.recordedAt;
        this.createdAt = builder.createdAt;
        prePersist();
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public BigDecimal getCashPrice() {
        return cashPrice;
    }

    public BigDecimal getInstallmentPrice() {
        return installmentPrice;
    }

    public int getInstallmentCount() {
        return installmentCount;
    }

    public String getCoupon() {
        return coupon;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public boolean isInStock() {
        return inStock;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getExternalId() {
        return externalId;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private UUID id;
        private UUID productId;
        private UUID storeId;
        private String storeName;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private BigDecimal cashPrice;
        private BigDecimal installmentPrice;
        private int installmentCount;
        private String coupon;
        private BigDecimal couponDiscount;
        private BigDecimal shippingCost;
        private boolean inStock;
        private boolean available;
        private String category;
        private String brand;
        private String model;
        private String externalId;
        private Instant recordedAt;
        private Instant createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder productId(UUID productId) {
            this.productId = productId;
            return this;
        }

        public Builder storeId(UUID storeId) {
            this.storeId = storeId;
            return this;
        }

        public Builder storeName(String storeName) {
            this.storeName = storeName;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder originalPrice(BigDecimal originalPrice) {
            this.originalPrice = originalPrice;
            return this;
        }

        public Builder cashPrice(BigDecimal cashPrice) {
            this.cashPrice = cashPrice;
            return this;
        }

        public Builder installmentPrice(BigDecimal installmentPrice) {
            this.installmentPrice = installmentPrice;
            return this;
        }

        public Builder installmentCount(int installmentCount) {
            this.installmentCount = installmentCount;
            return this;
        }

        public Builder coupon(String coupon) {
            this.coupon = coupon;
            return this;
        }

        public Builder couponDiscount(BigDecimal couponDiscount) {
            this.couponDiscount = couponDiscount;
            return this;
        }

        public Builder shippingCost(BigDecimal shippingCost) {
            this.shippingCost = shippingCost;
            return this;
        }

        public Builder inStock(boolean inStock) {
            this.inStock = inStock;
            return this;
        }

        public Builder available(boolean available) {
            this.available = available;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder recordedAt(Instant recordedAt) {
            this.recordedAt = recordedAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PriceRecord build() {
            if (productId == null) {
                throw new IllegalStateException("productId must not be null");
            }
            if (storeId == null) {
                throw new IllegalStateException("storeId must not be null");
            }
            if (storeName == null) {
                throw new IllegalStateException("storeName must not be null");
            }
            if (price == null) {
                throw new IllegalStateException("price must not be null");
            }
            if (recordedAt == null) {
                throw new IllegalStateException("recordedAt must not be null");
            }
            return new PriceRecord(this);
        }
    }
}
