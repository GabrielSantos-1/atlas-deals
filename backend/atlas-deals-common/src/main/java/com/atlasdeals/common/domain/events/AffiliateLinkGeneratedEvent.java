package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.ProductId;
import java.util.Map;

public class AffiliateLinkGeneratedEvent extends BaseDomainEvent {
    private final ProductId productId;
    private final String storeName;
    private final String originalUrl;
    private final String affiliateUrl;
    private final String affiliateNetwork;
    private final String campaignId;
    private final Map<String, String> trackingParams;

    public AffiliateLinkGeneratedEvent(ProductId productId, String storeName, String originalUrl,
                                       String affiliateUrl, String affiliateNetwork,
                                       String campaignId, Map<String, String> trackingParams) {
        this.productId = productId;
        this.storeName = storeName;
        this.originalUrl = originalUrl;
        this.affiliateUrl = affiliateUrl;
        this.affiliateNetwork = affiliateNetwork;
        this.campaignId = campaignId;
        this.trackingParams = trackingParams;
    }

    @Override
    public String getEventType() { return "affiliate.link.generated"; }

    public ProductId getProductId() { return productId; }
    public String getStoreName() { return storeName; }
    public String getOriginalUrl() { return originalUrl; }
    public String getAffiliateUrl() { return affiliateUrl; }
    public String getAffiliateNetwork() { return affiliateNetwork; }
    public String getCampaignId() { return campaignId; }
    public Map<String, String> getTrackingParams() { return trackingParams; }
}
