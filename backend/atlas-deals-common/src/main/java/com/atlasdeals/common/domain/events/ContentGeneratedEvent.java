package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.ProductId;
import java.math.BigDecimal;
import java.util.Map;

public class ContentGeneratedEvent extends BaseDomainEvent {
    private final ProductId productId;
    private final Map<String, String> contentByPlatform;
    private final String title;
    private final String description;
    private final String summary;
    private final String seoDescription;
    private final String hashtags;
    private final String category;
    private final String tags;
    private final String opportunityLevel;
    private final String technicalSummary;
    private final String pros;
    private final String cons;
    private final String recommendedProfile;
    private final String imageUrl;
    private final String storeName;
    private final BigDecimal price;
    private final BigDecimal originalPrice;

    public ContentGeneratedEvent(ProductId productId, Map<String, String> contentByPlatform,
                                 String title, String description, String summary,
                                 String seoDescription, String hashtags, String category,
                                 String tags, String opportunityLevel, String technicalSummary,
                                 String pros, String cons, String recommendedProfile) {
        this(productId, contentByPlatform, title, description, summary, seoDescription,
             hashtags, category, tags, opportunityLevel, technicalSummary, pros, cons,
             recommendedProfile, null, null, null, null);
    }

    public ContentGeneratedEvent(ProductId productId, Map<String, String> contentByPlatform,
                                 String title, String description, String summary,
                                 String seoDescription, String hashtags, String category,
                                 String tags, String opportunityLevel, String technicalSummary,
                                 String pros, String cons, String recommendedProfile,
                                 String imageUrl, String storeName,
                                 BigDecimal price, BigDecimal originalPrice) {
        this.productId = productId;
        this.contentByPlatform = contentByPlatform;
        this.title = title;
        this.description = description;
        this.summary = summary;
        this.seoDescription = seoDescription;
        this.hashtags = hashtags;
        this.category = category;
        this.tags = tags;
        this.opportunityLevel = opportunityLevel;
        this.technicalSummary = technicalSummary;
        this.pros = pros;
        this.cons = cons;
        this.recommendedProfile = recommendedProfile;
        this.imageUrl = imageUrl;
        this.storeName = storeName;
        this.price = price;
        this.originalPrice = originalPrice;
    }

    @Override
    public String getEventType() { return "content.generated"; }

    public ProductId getProductId() { return productId; }
    public Map<String, String> getContentByPlatform() { return contentByPlatform; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSummary() { return summary; }
    public String getSeoDescription() { return seoDescription; }
    public String getHashtags() { return hashtags; }
    public String getCategory() { return category; }
    public String getTags() { return tags; }
    public String getOpportunityLevel() { return opportunityLevel; }
    public String getTechnicalSummary() { return technicalSummary; }
    public String getPros() { return pros; }
    public String getCons() { return cons; }
    public String getRecommendedProfile() { return recommendedProfile; }
    public String getImageUrl() { return imageUrl; }
    public String getStoreName() { return storeName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
}
