package com.atlasdeals.analytics.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_metrics")
public class DailyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 100)
    private String platform;

    @Column(length = 100)
    private String category;

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "total_clicks", nullable = false)
    private long totalClicks;

    @Column(name = "total_conversions", nullable = false)
    private long totalConversions;

    @Column(name = "total_revenue", precision = 14, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_views", nullable = false)
    private long totalViews;

    @Column(name = "total_shares", nullable = false)
    private long totalShares;

    @Column(nullable = false)
    private double ctr;

    @Column(name = "conversion_rate", nullable = false)
    private double conversionRate;

    @Column(name = "revenue_per_click", precision = 14, scale = 2)
    private BigDecimal revenuePerClick;

    @Column(name = "unique_products", nullable = false)
    private long uniqueProducts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public DailyMetric() {
    }

    public DailyMetric(UUID id, LocalDate date, String platform, String category,
                       String storeName, long totalClicks, long totalConversions,
                       BigDecimal totalRevenue, long totalViews, long totalShares,
                       double ctr, double conversionRate, BigDecimal revenuePerClick,
                       long uniqueProducts) {
        this.id = id;
        this.date = date;
        this.platform = platform;
        this.category = category;
        this.storeName = storeName;
        this.totalClicks = totalClicks;
        this.totalConversions = totalConversions;
        this.totalRevenue = totalRevenue;
        this.totalViews = totalViews;
        this.totalShares = totalShares;
        this.ctr = ctr;
        this.conversionRate = conversionRate;
        this.revenuePerClick = revenuePerClick;
        this.uniqueProducts = uniqueProducts;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public long getTotalConversions() {
        return totalConversions;
    }

    public void setTotalConversions(long totalConversions) {
        this.totalConversions = totalConversions;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(long totalViews) {
        this.totalViews = totalViews;
    }

    public long getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(long totalShares) {
        this.totalShares = totalShares;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public BigDecimal getRevenuePerClick() {
        return revenuePerClick;
    }

    public void setRevenuePerClick(BigDecimal revenuePerClick) {
        this.revenuePerClick = revenuePerClick;
    }

    public long getUniqueProducts() {
        return uniqueProducts;
    }

    public void setUniqueProducts(long uniqueProducts) {
        this.uniqueProducts = uniqueProducts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
