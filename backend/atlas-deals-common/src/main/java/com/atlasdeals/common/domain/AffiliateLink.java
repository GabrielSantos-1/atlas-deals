package com.atlasdeals.common.domain;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class AffiliateLink extends ValueObject {
    private final ProductId productId;
    private final StoreId storeId;
    private final AffiliateNetwork network;
    private final URL originalUrl;
    private final URL affiliateUrl;
    private final String campaignId;
    private final String source;
    private final Map<String, String> trackingParams;

    public AffiliateLink(ProductId productId, StoreId storeId, AffiliateNetwork network,
                         URL originalUrl, URL affiliateUrl, String campaignId, String source,
                         Map<String, String> trackingParams) {
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.storeId = Objects.requireNonNull(storeId, "storeId must not be null");
        this.network = Objects.requireNonNull(network, "network must not be null");
        this.originalUrl = Objects.requireNonNull(originalUrl, "originalUrl must not be null");
        this.affiliateUrl = Objects.requireNonNull(affiliateUrl, "affiliateUrl must not be null");
        this.campaignId = campaignId;
        this.source = source;
        this.trackingParams = trackingParams != null
            ? Collections.unmodifiableMap(trackingParams)
            : Map.of();
    }

    public AffiliateLink withCampaign(String campaignId) {
        return new AffiliateLink(productId, storeId, network, originalUrl, affiliateUrl,
                                 campaignId, source, trackingParams);
    }

    public AffiliateLink withSource(String source) {
        return new AffiliateLink(productId, storeId, network, originalUrl, affiliateUrl,
                                 campaignId, source, trackingParams);
    }

    public AffiliateLink withExtraParam(String key, String value) {
        var newParams = new java.util.HashMap<>(this.trackingParams);
        newParams.put(key, value);
        return new AffiliateLink(productId, storeId, network, originalUrl, affiliateUrl,
                                 campaignId, source, newParams);
    }

    public ProductId getProductId() { return productId; }
    public StoreId getStoreId() { return storeId; }
    public AffiliateNetwork getNetwork() { return network; }
    public URL getOriginalUrl() { return originalUrl; }
    public URL getAffiliateUrl() { return affiliateUrl; }
    public String getCampaignId() { return campaignId; }
    public String getSource() { return source; }
    public Map<String, String> getTrackingParams() { return trackingParams; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AffiliateLink that = (AffiliateLink) o;
        return affiliateUrl.equals(that.affiliateUrl);
    }

    @Override
    public int hashCode() {
        return affiliateUrl.hashCode();
    }

    @Override
    public String toString() {
        return affiliateUrl.toString();
    }
}
