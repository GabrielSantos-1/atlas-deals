package com.atlasdeals.promotionengine.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "promotions", indexes = {
    @Index(name = "idx_promotions_product_id", columnList = "productId"),
    @Index(name = "idx_promotions_status", columnList = "status"),
    @Index(name = "idx_promotions_category_status", columnList = "category, status"),
    @Index(name = "idx_promotions_score", columnList = "score DESC"),
    @Index(name = "idx_promotions_published_at", columnList = "publishedAt")
})
public class Promotion {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 200)
    private String storeName;

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(length = 2000)
    private String imageUrl;

    @Column(nullable = false)
    private boolean inStock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionStatus status;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    private Instant detectedAt;

    private Instant publishedAt;

    private Instant expiredAt;

    @Column(nullable = false)
    private int totalClicks;

    @Column(nullable = false)
    private int totalConversions;

    @Column(precision = 12, scale = 2)
    private BigDecimal revenue;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Promotion() {
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (detectedAt == null) {
            detectedAt = Instant.now();
        }
        if (status == null) {
            status = PromotionStatus.DETECTED;
        }
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    private Promotion(Builder builder) {
        this.id = builder.id;
        this.productId = builder.productId;
        this.title = builder.title;
        this.currentPrice = builder.currentPrice;
        this.originalPrice = builder.originalPrice;
        this.discountPercentage = builder.discountPercentage;
        this.score = builder.score;
        this.category = builder.category;
        this.storeName = builder.storeName;
        this.storeId = builder.storeId;
        this.url = builder.url;
        this.imageUrl = builder.imageUrl;
        this.inStock = builder.inStock;
        this.status = builder.status;
        this.tags = builder.tags;
        this.reason = builder.reason;
        this.detectedAt = builder.detectedAt;
        this.publishedAt = builder.publishedAt;
        this.expiredAt = builder.expiredAt;
        this.totalClicks = builder.totalClicks;
        this.totalConversions = builder.totalConversions;
        this.revenue = builder.revenue;
        prePersist();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void approve() {
        this.status = PromotionStatus.APPROVED;
    }

    public void reject() {
        this.status = PromotionStatus.REJECTED;
    }

    public void queue() {
        this.status = PromotionStatus.QUEUED;
    }

    public void process() {
        this.status = PromotionStatus.PROCESSING;
    }

    public void publish() {
        this.status = PromotionStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void expire() {
        this.status = PromotionStatus.EXPIRED;
        this.expiredAt = Instant.now();
    }

    public void incrementClicks() {
        this.totalClicks++;
    }

    public void addConversion(BigDecimal conversionRevenue) {
        this.totalConversions++;
        if (this.revenue == null) {
            this.revenue = BigDecimal.ZERO;
        }
        this.revenue = this.revenue.add(conversionRevenue).setScale(2, RoundingMode.HALF_EVEN);
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getTitle() { return title; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getScore() { return score; }
    public String getCategory() { return category; }
    public String getStoreName() { return storeName; }
    public UUID getStoreId() { return storeId; }
    public String getUrl() { return url; }
    public String getImageUrl() { return imageUrl; }
    public boolean isInStock() { return inStock; }
    public PromotionStatus getStatus() { return status; }
    public List<String> getTags() { return tags; }
    public String getReason() { return reason; }
    public Instant getDetectedAt() { return detectedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getExpiredAt() { return expiredAt; }
    public int getTotalClicks() { return totalClicks; }
    public int getTotalConversions() { return totalConversions; }
    public BigDecimal getRevenue() { return revenue; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private UUID id;
        private UUID productId;
        private String title;
        private BigDecimal currentPrice;
        private BigDecimal originalPrice;
        private BigDecimal discountPercentage;
        private BigDecimal score;
        private String category;
        private String storeName;
        private UUID storeId;
        private String url;
        private String imageUrl;
        private boolean inStock;
        private PromotionStatus status = PromotionStatus.DETECTED;
        private List<String> tags;
        private String reason;
        private Instant detectedAt;
        private Instant publishedAt;
        private Instant expiredAt;
        private int totalClicks;
        private int totalConversions;
        private BigDecimal revenue;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder productId(UUID productId) { this.productId = productId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder currentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; return this; }
        public Builder originalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; return this; }
        public Builder discountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; return this; }
        public Builder score(BigDecimal score) { this.score = score; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder storeName(String storeName) { this.storeName = storeName; return this; }
        public Builder storeId(UUID storeId) { this.storeId = storeId; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder inStock(boolean inStock) { this.inStock = inStock; return this; }
        public Builder status(PromotionStatus status) { this.status = status; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder detectedAt(Instant detectedAt) { this.detectedAt = detectedAt; return this; }
        public Builder publishedAt(Instant publishedAt) { this.publishedAt = publishedAt; return this; }
        public Builder expiredAt(Instant expiredAt) { this.expiredAt = expiredAt; return this; }
        public Builder totalClicks(int totalClicks) { this.totalClicks = totalClicks; return this; }
        public Builder totalConversions(int totalConversions) { this.totalConversions = totalConversions; return this; }
        public Builder revenue(BigDecimal revenue) { this.revenue = revenue; return this; }

        public Promotion build() {
            if (productId == null) throw new IllegalStateException("productId must not be null");
            if (title == null) throw new IllegalStateException("title must not be null");
            if (currentPrice == null) throw new IllegalStateException("currentPrice must not be null");
            if (storeName == null) throw new IllegalStateException("storeName must not be null");
            if (storeId == null) throw new IllegalStateException("storeId must not be null");
            if (url == null) throw new IllegalStateException("url must not be null");
            return new Promotion(this);
        }
    }
}
