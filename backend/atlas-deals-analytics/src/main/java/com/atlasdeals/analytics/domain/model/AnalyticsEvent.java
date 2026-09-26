package com.atlasdeals.analytics.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "analytics_events")
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "click_id")
    private UUID clickId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_title", length = 500)
    private String productTitle;

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 100)
    private String platform;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "campaign_id", length = 255)
    private String campaignId;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(precision = 14, scale = 2)
    private BigDecimal revenue;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean converted;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public AnalyticsEvent() {
    }

    public AnalyticsEvent(UUID id, UUID clickId, UUID productId, String productTitle,
                          String storeName, String category, String platform, String source,
                          String campaignId, String deviceType, String eventType,
                          BigDecimal revenue, String currency, boolean converted,
                          String metadata, Instant eventTime) {
        this.id = id;
        this.clickId = clickId;
        this.productId = productId;
        this.productTitle = productTitle;
        this.storeName = storeName;
        this.category = category;
        this.platform = platform;
        this.source = source;
        this.campaignId = campaignId;
        this.deviceType = deviceType;
        this.eventType = eventType;
        this.revenue = revenue;
        this.currency = currency;
        this.converted = converted;
        this.metadata = metadata;
        this.eventTime = eventTime;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClickId() {
        return clickId;
    }

    public void setClickId(UUID clickId) {
        this.clickId = clickId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isConverted() {
        return converted;
    }

    public void setConverted(boolean converted) {
        this.converted = converted;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
