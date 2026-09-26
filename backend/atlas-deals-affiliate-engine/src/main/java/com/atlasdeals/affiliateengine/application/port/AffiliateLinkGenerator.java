package com.atlasdeals.affiliateengine.application.port;

import com.atlasdeals.common.domain.AffiliateNetwork;

public interface AffiliateLinkGenerator {

    String generateLink(String originalUrl, String productId, String campaignId);

    AffiliateNetwork getNetwork();

    boolean supports(String storeName);
}
