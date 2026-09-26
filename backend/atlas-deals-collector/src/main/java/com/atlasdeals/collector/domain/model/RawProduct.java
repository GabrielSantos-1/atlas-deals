package com.atlasdeals.collector.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "raw_products")
public class RawProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "store_id", length = 100)
    private String storeId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "current_price", precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(length = 3)
    private String currency;

    @Column(name = "product_url", length = 2048)
    private String productUrl;

    @Column(name = "product_url_hash")
    private int productUrlHash;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(length = 100)
    private String category;

    @Column(length = 255)
    private String brand;

    @Column(length = 255)
    private String model;

    @Column(length = 100)
    private String sku;

    @Column(length = 13)
    private String ean;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock;

    @Column(nullable = false)
    private boolean available;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data")
    private String rawData;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public RawProduct() {
    }

    public RawProduct(UUID id, String externalId, String storeName, String storeId, String title,
                      String description, BigDecimal currentPrice, BigDecimal originalPrice,
                      String currency, String productUrl, String imageUrl, String category,
                      String brand, String model, String sku, String ean, boolean inStock,
                      boolean available, String rawData, Instant collectedAt) {
        this.id = id;
        this.externalId = externalId;
        this.storeName = storeName;
        this.storeId = storeId;
        this.title = title;
        this.description = description;
        this.currentPrice = currentPrice;
        this.originalPrice = originalPrice;
        this.currency = currency;
        this.productUrl = productUrl;
        this.productUrlHash = productUrl != null ? productUrl.hashCode() : 0;
        this.imageUrl = imageUrl;
        this.category = category;
        this.brand = brand;
        this.model = model;
        this.sku = sku;
        this.ean = ean;
        this.inStock = inStock;
        this.available = available;
        this.rawData = rawData;
        this.collectedAt = collectedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (collectedAt == null) {
            collectedAt = Instant.now();
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (productUrl != null) {
            productUrlHash = productUrl.hashCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (productUrl != null) {
            productUrlHash = productUrl.hashCode();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public int getProductUrlHash() {
        return productUrlHash;
    }

    public void setProductUrlHash(int productUrlHash) {
        this.productUrlHash = productUrlHash;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
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
