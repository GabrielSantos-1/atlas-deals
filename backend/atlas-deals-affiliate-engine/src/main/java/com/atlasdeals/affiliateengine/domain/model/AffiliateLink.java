package com.atlasdeals.affiliateengine.domain.model;

import com.atlasdeals.common.domain.AffiliateNetwork;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "affiliate_links", uniqueConstraints = {
    @UniqueConstraint(name = "uq_product_network", columnNames = {"productId", "network"})
}, indexes = {
    @Index(name = "idx_affiliate_links_product_id", columnList = "productId"),
    @Index(name = "idx_affiliate_links_network", columnList = "network"),
    @Index(name = "idx_affiliate_links_last_clicked", columnList = "lastClickedAt")
})
public class AffiliateLink {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 200)
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AffiliateNetwork network;

    @Column(nullable = false, columnDefinition = "text")
    private String originalUrl;

    @Column(nullable = false, columnDefinition = "text")
    private String affiliateUrl;

    @Column(length = 200)
    private String campaignId;

    @Column(length = 200)
    private String partnerId;

    @Column(nullable = false)
    private int clickCount;

    @Column(nullable = false)
    private int conversionCount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal revenue;

    private Instant lastClickedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public AffiliateLink() {
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
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

    private AffiliateLink(Builder builder) {
        this.id = builder.id;
        this.productId = builder.productId;
        this.storeName = builder.storeName;
        this.network = builder.network;
        this.originalUrl = builder.originalUrl;
        this.affiliateUrl = builder.affiliateUrl;
        this.campaignId = builder.campaignId;
        this.partnerId = builder.partnerId;
        this.clickCount = builder.clickCount;
        this.conversionCount = builder.conversionCount;
        this.revenue = builder.revenue;
        this.lastClickedAt = builder.lastClickedAt;
        prePersist();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void registerClick() {
        this.clickCount++;
        this.lastClickedAt = Instant.now();
    }

    public void registerConversion(BigDecimal conversionRevenue) {
        this.conversionCount++;
        if (this.revenue == null) {
            this.revenue = BigDecimal.ZERO;
        }
        this.revenue = this.revenue.add(conversionRevenue).setScale(2, RoundingMode.HALF_EVEN);
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getStoreName() { return storeName; }
    public AffiliateNetwork getNetwork() { return network; }
    public String getOriginalUrl() { return originalUrl; }
    public String getAffiliateUrl() { return affiliateUrl; }
    public String getCampaignId() { return campaignId; }
    public String getPartnerId() { return partnerId; }
    public int getClickCount() { return clickCount; }
    public int getConversionCount() { return conversionCount; }
    public BigDecimal getRevenue() { return revenue; }
    public Instant getLastClickedAt() { return lastClickedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private UUID id;
        private UUID productId;
        private String storeName;
        private AffiliateNetwork network;
        private String originalUrl;
        private String affiliateUrl;
        private String campaignId;
        private String partnerId;
        private int clickCount;
        private int conversionCount;
        private BigDecimal revenue = BigDecimal.ZERO;
        private Instant lastClickedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder productId(UUID productId) { this.productId = productId; return this; }
        public Builder storeName(String storeName) { this.storeName = storeName; return this; }
        public Builder network(AffiliateNetwork network) { this.network = network; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder affiliateUrl(String affiliateUrl) { this.affiliateUrl = affiliateUrl; return this; }
        public Builder campaignId(String campaignId) { this.campaignId = campaignId; return this; }
        public Builder partnerId(String partnerId) { this.partnerId = partnerId; return this; }
        public Builder clickCount(int clickCount) { this.clickCount = clickCount; return this; }
        public Builder conversionCount(int conversionCount) { this.conversionCount = conversionCount; return this; }
        public Builder revenue(BigDecimal revenue) { this.revenue = revenue; return this; }
        public Builder lastClickedAt(Instant lastClickedAt) { this.lastClickedAt = lastClickedAt; return this; }

        public AffiliateLink build() {
            if (productId == null) throw new IllegalStateException("productId must not be null");
            if (storeName == null) throw new IllegalStateException("storeName must not be null");
            if (network == null) throw new IllegalStateException("network must not be null");
            if (originalUrl == null) throw new IllegalStateException("originalUrl must not be null");
            if (affiliateUrl == null) throw new IllegalStateException("affiliateUrl must not be null");
            return new AffiliateLink(this);
        }
    }
}
