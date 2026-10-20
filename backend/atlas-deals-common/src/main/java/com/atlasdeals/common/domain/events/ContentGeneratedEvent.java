package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.ProductId;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
    isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentGeneratedEvent extends BaseDomainEvent {
    private ProductId productId;
    private Map<String, String> contentByPlatform;
    private String title;
    private String description;
    private String summary;
    private String seoDescription;
    private String hashtags;
    private String category;
    private String tags;
    private String opportunityLevel;
    private String technicalSummary;
    private String pros;
    private String cons;
    private String recommendedProfile;
    private String imageUrl;
    private String storeName;
    private BigDecimal price;
    private BigDecimal originalPrice;

    /**
     * Required by Jackson to rebuild the event from a message payload. The event identity
     * (eventId/occurredOn) is restored from the JSON instead of being regenerated.
     */
    protected ContentGeneratedEvent() {
    }

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
