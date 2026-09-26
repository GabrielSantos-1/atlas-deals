package com.atlasdeals.affiliateengine.application.dto;

import com.atlasdeals.affiliateengine.domain.model.AffiliateLink;
import com.atlasdeals.common.domain.AffiliateNetwork;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AffiliateLinkResponse {

    private final UUID id;
    private final UUID productId;
    private final String storeName;
    private final AffiliateNetwork network;
    private final String originalUrl;
    private final String affiliateUrl;
    private final String campaignId;
    private final String partnerId;
    private final int clickCount;
    private final int conversionCount;
    private final BigDecimal revenue;
    private final Instant lastClickedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    public AffiliateLinkResponse(AffiliateLink link) {
        this.id = link.getId();
        this.productId = link.getProductId();
        this.storeName = link.getStoreName();
        this.network = link.getNetwork();
        this.originalUrl = link.getOriginalUrl();
        this.affiliateUrl = link.getAffiliateUrl();
        this.campaignId = link.getCampaignId();
        this.partnerId = link.getPartnerId();
        this.clickCount = link.getClickCount();
        this.conversionCount = link.getConversionCount();
        this.revenue = link.getRevenue();
        this.lastClickedAt = link.getLastClickedAt();
        this.createdAt = link.getCreatedAt();
        this.updatedAt = link.getUpdatedAt();
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
}
