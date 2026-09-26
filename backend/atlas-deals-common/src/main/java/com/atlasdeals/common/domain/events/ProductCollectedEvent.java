package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.StoreId;
import java.math.BigDecimal;
import java.time.Instant;

public class ProductCollectedEvent extends BaseDomainEvent {
    private final ProductId productId;
    private final StoreId storeId;
    private final String storeName;
    private final String externalId;
    private final String title;
    private final Money price;
    private final Money originalPrice;
    private final String currency;
    private final String url;
    private final String imageUrl;
    private final String category;
    private final String brand;
    private final String model;
    private final boolean inStock;
    private final String sku;
    private final String ean;
    private final Instant collectedAt;

    public ProductCollectedEvent(ProductId productId, StoreId storeId, String storeName,
                                 String externalId, String title, Money price, Money originalPrice,
                                 String currency, String url, String imageUrl, String category,
                                 String brand, String model, boolean inStock, String sku,
                                 String ean, Instant collectedAt) {
        this.productId = productId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.externalId = externalId;
        this.title = title;
        this.price = price;
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
        this.collectedAt = collectedAt;
    }

    @Override
    public String getEventType() {
        return "product.collected";
    }

    public ProductId getProductId() { return productId; }
    public StoreId getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public String getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public Money getPrice() { return price; }
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
    public Instant getCollectedAt() { return collectedAt; }

    public BigDecimal getDiscountPercentage() {
        if (originalPrice == null || originalPrice.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.discountPercentage(originalPrice);
    }
}
