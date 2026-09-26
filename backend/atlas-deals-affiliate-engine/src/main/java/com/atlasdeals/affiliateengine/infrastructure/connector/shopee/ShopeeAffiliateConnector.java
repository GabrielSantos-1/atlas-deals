package com.atlasdeals.affiliateengine.infrastructure.connector.shopee;

import com.atlasdeals.affiliateengine.application.port.AffiliateLinkGenerator;
import com.atlasdeals.affiliateengine.infrastructure.config.AffiliateProperties;
import com.atlasdeals.affiliateengine.infrastructure.connector.BaseAffiliateConnector;
import com.atlasdeals.common.domain.AffiliateNetwork;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShopeeAffiliateConnector extends BaseAffiliateConnector implements AffiliateLinkGenerator {

    private static final Logger log = LoggerFactory.getLogger(ShopeeAffiliateConnector.class);
    private static final List<String> SUPPORTED_STORES = List.of("shopee", "shopee br", "shopee brasil");
    private static final String DIRECT_URL = "https://shopee.com.br/product/%s";
    private static final String API_URL = "https://affiliate.shopee.com.br/generate";

    private final AffiliateProperties properties;

    public ShopeeAffiliateConnector(AffiliateProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateLink(String originalUrl, String productId, String campaignId) {
        String partnerId = properties.getShopee().getPartnerId();
        if (partnerId == null || partnerId.isBlank()) {
            log.warn("Shopee partnerId not configured, using direct URL without affiliate params");
            return String.format(DIRECT_URL, productId);
        }
        return buildUrl(String.format(DIRECT_URL, productId),
            "af_id", partnerId,
            "campaign", campaignId != null ? campaignId : properties.getDefaultCampaign());
    }

    @Override
    public AffiliateNetwork getNetwork() {
        return AffiliateNetwork.SHOPEE;
    }

    @Override
    public boolean supports(String storeName) {
        return SUPPORTED_STORES.stream().anyMatch(s -> storeName.contains(s));
    }
}
