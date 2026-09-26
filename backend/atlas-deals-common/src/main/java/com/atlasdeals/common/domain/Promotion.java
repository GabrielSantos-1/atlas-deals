package com.atlasdeals.common.domain;

import com.atlasdeals.common.domain.events.PromotionDetectedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class Promotion extends AggregateRoot<java.util.UUID> {
    private final ProductId productId;
    private final String title;
    private final Money currentPrice;
    private final Money originalPrice;
    private final BigDecimal discountPercentage;
    private final BigDecimal score;
    private final String category;
    private final String storeName;
    private final String url;
    private final String imageUrl;
    private final boolean inStock;
    private final List<String> tags;
    private final String reason;
    private PromotionStatus status;
    private Instant detectedAt;
    private Instant publishedAt;

    public Promotion(java.util.UUID id, ProductId productId, String title, Money currentPrice,
                     Money originalPrice, BigDecimal discountPercentage, BigDecimal score,
                     String category, String storeName, String url, String imageUrl,
                     boolean inStock, List<String> tags, String reason) {
        super(id);
        this.productId = productId;
        this.title = title;
        this.currentPrice = currentPrice;
        this.originalPrice = originalPrice;
        this.discountPercentage = discountPercentage;
        this.score = score;
        this.category = category;
        this.storeName = storeName;
        this.url = url;
        this.imageUrl = imageUrl;
        this.inStock = inStock;
        this.tags = tags;
        this.reason = reason;
        this.status = PromotionStatus.DETECTED;
        this.detectedAt = Instant.now();
    }

    public static Promotion detect(Product product, BigDecimal discountPercentage, BigDecimal score,
                                   List<String> tags, String reason) {
        java.util.UUID id = java.util.UUID.randomUUID();
        Promotion promotion = new Promotion(id, product.getId(), product.getTitle(),
            product.getCurrentPrice(), product.getOriginalPrice(), discountPercentage, score,
            product.getCategory(), product.getStoreName(), product.getUrl(), product.getImageUrl(),
            product.isInStock(), tags, reason);
        promotion.registerEvent(new PromotionDetectedEvent(
            product.getId(), product.getTitle(), product.getCurrentPrice(), product.getOriginalPrice(),
            discountPercentage, score, product.getCategory(), product.getStoreName(), product.getUrl(),
            product.getImageUrl(), product.isInStock(), tags, reason));
        return promotion;
    }

    public void publish() {
        this.status = PromotionStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void expire() {
        this.status = PromotionStatus.EXPIRED;
    }

    public ProductId getProductId() { return productId; }
    public String getTitle() { return title; }
    public Money getCurrentPrice() { return currentPrice; }
    public Money getOriginalPrice() { return originalPrice; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getScore() { return score; }
    public String getCategory() { return category; }
    public String getStoreName() { return storeName; }
    public String getUrl() { return url; }
    public String getImageUrl() { return imageUrl; }
    public boolean isInStock() { return inStock; }
    public List<String> getTags() { return tags; }
    public String getReason() { return reason; }
    public PromotionStatus getStatus() { return status; }
    public Instant getDetectedAt() { return detectedAt; }
    public Instant getPublishedAt() { return publishedAt; }

    public enum PromotionStatus {
        DETECTED, QUEUED, PROCESSING, PUBLISHED, EXPIRED, FAILED
    }
}
