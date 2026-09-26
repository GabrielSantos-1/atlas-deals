package com.atlasdeals.affiliateengine.infrastructure.connector.kabum;

import com.atlasdeals.affiliateengine.application.port.AffiliateLinkGenerator;
import com.atlasdeals.affiliateengine.infrastructure.config.AffiliateProperties;
import com.atlasdeals.affiliateengine.infrastructure.connector.BaseAffiliateConnector;
import com.atlasdeals.common.domain.AffiliateNetwork;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KabumAffiliateConnector extends BaseAffiliateConnector implements AffiliateLinkGenerator {

    private static final Logger log = LoggerFactory.getLogger(KabumAffiliateConnector.class);
    private static final List<String> SUPPORTED_STORES = List.of("kabum");
    private static final String BASE_URL = "https://www.kabum.com.br/produto/%s";

    private final AffiliateProperties properties;

    public KabumAffiliateConnector(AffiliateProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateLink(String originalUrl, String productId, String campaignId) {
        String partnerId = properties.getKabum().getPartnerId();
        if (partnerId == null || partnerId.isBlank()) {
            log.warn("Kabum partnerId not configured, returning direct URL");
            return String.format(BASE_URL, productId);
        }
        return buildUrl(String.format(BASE_URL, productId),
            "afid", partnerId,
            "campaign", campaignId != null ? campaignId : properties.getDefaultCampaign());
    }

    @Override
    public AffiliateNetwork getNetwork() {
        return AffiliateNetwork.KABUM;
    }

    @Override
    public boolean supports(String storeName) {
        return SUPPORTED_STORES.stream().anyMatch(s -> storeName.contains(s));
    }
}
