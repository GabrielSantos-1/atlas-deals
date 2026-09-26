package com.atlasdeals.affiliateengine.infrastructure.connector.aliexpress;

import com.atlasdeals.affiliateengine.application.port.AffiliateLinkGenerator;
import com.atlasdeals.affiliateengine.infrastructure.config.AffiliateProperties;
import com.atlasdeals.affiliateengine.infrastructure.connector.BaseAffiliateConnector;
import com.atlasdeals.common.domain.AffiliateNetwork;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AliexpressAffiliateConnector extends BaseAffiliateConnector implements AffiliateLinkGenerator {

    private static final Logger log = LoggerFactory.getLogger(AliexpressAffiliateConnector.class);
    private static final List<String> SUPPORTED_STORES = List.of("aliexpress", "ali express", "aliexpress br");
    private static final String DEEPLINK_URL = "https://s.click.aliexpress.com/deeplink";

    private final AffiliateProperties properties;

    public AliexpressAffiliateConnector(AffiliateProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateLink(String originalUrl, String productId, String campaignId) {
        String affiliateTag = properties.getAliexpress().getAffiliateTag();
        if (affiliateTag == null || affiliateTag.isBlank()) {
            log.warn("AliExpress affiliateTag not configured, returning original URL");
            return originalUrl;
        }
        return buildUrl(DEEPLINK_URL,
            "aff_track_key", affiliateTag,
            "aff_platform", "atlas-deals",
            "product_id", productId,
            "campaign", campaignId != null ? campaignId : properties.getDefaultCampaign());
    }

    @Override
    public AffiliateNetwork getNetwork() {
        return AffiliateNetwork.ALIEXPRESS;
    }

    @Override
    public boolean supports(String storeName) {
        return SUPPORTED_STORES.stream().anyMatch(s -> storeName.contains(s));
    }
}
