package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import java.math.BigDecimal;
import java.util.List;

public class PromotionDetectedEvent extends BaseDomainEvent {
    private final ProductId productId;
    private final String title;
    private final Money currentPrice;
    private final Money originalPrice;
    private final BigDecimal discountPercentage;
    private final BigDecimal promotionScore;
    private final String category;
    private final String storeName;
    private final String url;
    private final String imageUrl;
    private final boolean inStock;
    private final List<String> tags;
    private final String reason;

    public PromotionDetectedEvent(ProductId productId, String title, Money currentPrice,
                                  Money originalPrice, BigDecimal discountPercentage,
                                  BigDecimal promotionScore, String category, String storeName,
                                  String url, String imageUrl, boolean inStock,
                                  List<String> tags, String reason) {
        this.productId = productId;
        this.title = title;
        this.currentPrice = currentPrice;
        this.originalPrice = originalPrice;
        this.discountPercentage = discountPercentage;
        this.promotionScore = promotionScore;
        this.category = category;
        this.storeName = storeName;
        this.url = url;
        this.imageUrl = imageUrl;
        this.inStock = inStock;
        this.tags = tags;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "promotion.detected";
    }

    public ProductId getProductId() { return productId; }
    public String getTitle() { return title; }
    public Money getCurrentPrice() { return currentPrice; }
    public Money getOriginalPrice() { return originalPrice; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getPromotionScore() { return promotionScore; }
    public String getCategory() { return category; }
    public String getStoreName() { return storeName; }
    public String getUrl() { return url; }
    public String getImageUrl() { return imageUrl; }
    public boolean isInStock() { return inStock; }
    public List<String> getTags() { return tags; }
    public String getReason() { return reason; }
}
