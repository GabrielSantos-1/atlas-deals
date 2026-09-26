package com.atlasdeals.common.domain;

import com.atlasdeals.common.domain.events.ProductCollectedEvent;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Product extends AggregateRoot<ProductId> {
    private StoreId storeId;
    private String storeName;
    private String externalId;
    private String title;
    private String description;
    private Money currentPrice;
    private Money originalPrice;
    private String currency;
    private String url;
    private String imageUrl;
    private String category;
    private String brand;
    private String model;
    private boolean inStock;
    private String sku;
    private String ean;
    private Money lowestPrice;
    private Instant lowestPriceRecordedAt;
    private Instant firstSeenAt;
    private Instant lastUpdatedAt;
    private Set<String> tags;
    private int totalClicks;
    private int totalConversions;
    private Money totalRevenue;

    public Product(ProductId id, StoreId storeId, String storeName, String externalId,
                   String title, Money currentPrice, Money originalPrice, String currency,
                   String url, String imageUrl, String category, String brand, String model,
                   boolean inStock, String sku, String ean) {
        super(id);
        this.storeId = storeId;
        this.storeName = storeName;
        this.externalId = externalId;
        this.title = title;
        this.currentPrice = currentPrice;
        this.originalPrice = originalPrice;
        this.currency = currency;
        this.url = url;
        this.imageUrl = imageUrl;
        this.category = category;
        this.brand = brand;
        this.model = model;
        this.inStock = inStock;
        this.sku = sku;
        this.ean = ean;
        this.lowestPrice = currentPrice;
        this.lowestPriceRecordedAt = Instant.now();
        this.firstSeenAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
        this.tags = new HashSet<>();
        this.totalClicks = 0;
        this.totalConversions = 0;
        this.totalRevenue = Money.zero(currency);
    }

    public static Product collect(ProductId id, StoreId storeId, String storeName, String externalId,
                                  String title, Money currentPrice, Money originalPrice, String currency,
                                  String url, String imageUrl, String category, String brand, String model,
                                  boolean inStock, String sku, String ean) {
        Product product = new Product(id, storeId, storeName, externalId, title, currentPrice,
                                      originalPrice, currency, url, imageUrl, category, brand, model,
                                      inStock, sku, ean);
        product.registerEvent(new ProductCollectedEvent(
            id, storeId, storeName, externalId, title, currentPrice, originalPrice,
            currency, url, imageUrl, category, brand, model, inStock, sku, ean, Instant.now()
        ));
        return product;
    }

    public void updatePrice(Money newPrice, Money newOriginalPrice, boolean inStock) {
        this.previousPrice = this.currentPrice;
        this.currentPrice = newPrice;
        this.originalPrice = newOriginalPrice;
        this.inStock = inStock;
        this.lastUpdatedAt = Instant.now();

        if (newPrice.isLessThan(this.lowestPrice)) {
            this.lowestPrice = newPrice;
            this.lowestPriceRecordedAt = Instant.now();
        }

        if (previousPrice != null && !previousPrice.equals(newPrice)) {
            registerEvent(new com.atlasdeals.common.domain.events.PriceUpdatedEvent(
                getId(), previousPrice, newPrice, Instant.now()
            ));
        }
    }

    public boolean isPromotion(BigDecimal minDiscountPercent) {
        if (originalPrice == null || originalPrice.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return false;
        }
        return currentPrice.discountPercentage(originalPrice)
            .compareTo(minDiscountPercent) >= 0;
    }

    public void registerClick() {
        this.totalClicks++;
    }

    public void registerConversion(Money revenue) {
        this.totalConversions++;
        this.totalRevenue = this.totalRevenue.add(revenue);
    }

    public double getConversionRate() {
        if (totalClicks == 0) return 0.0;
        return (double) totalConversions / totalClicks;
    }

    // Getters
    public StoreId getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public String getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Money getCurrentPrice() { return currentPrice; }
    public Money getOriginalPrice() { return originalPrice; }
    public String getCurrency() { return currency; }
    public String getUrl() { return url; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public boolean isInStock() { return inStock; }
    public String getSku() { return sku; }
    public String getEan() { return ean; }
    public Money getLowestPrice() { return lowestPrice; }
    public Instant getLowestPriceRecordedAt() { return lowestPriceRecordedAt; }
    public Instant getFirstSeenAt() { return firstSeenAt; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public Set<String> getTags() { return tags; }
    public int getTotalClicks() { return totalClicks; }
    public int getTotalConversions() { return totalConversions; }
    public Money getTotalRevenue() { return totalRevenue; }

    private Money previousPrice;

    public Money getPreviousPrice() { return previousPrice; }
}
