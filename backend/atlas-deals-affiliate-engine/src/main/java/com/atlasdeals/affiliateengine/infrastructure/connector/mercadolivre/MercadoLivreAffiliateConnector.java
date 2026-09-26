package com.atlasdeals.affiliateengine.infrastructure.connector.mercadolivre;

import com.atlasdeals.affiliateengine.application.port.AffiliateLinkGenerator;
import com.atlasdeals.affiliateengine.infrastructure.config.AffiliateProperties;
import com.atlasdeals.affiliateengine.infrastructure.connector.BaseAffiliateConnector;
import com.atlasdeals.common.domain.AffiliateNetwork;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MercadoLivreAffiliateConnector extends BaseAffiliateConnector implements AffiliateLinkGenerator {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreAffiliateConnector.class);
    private static final List<String> SUPPORTED_STORES = List.of(
        "mercadolivre", "mercado livre", "mercadolibre", "melipagamentos", "mercado pago");
    private static final String BASE_URL = "https://mercadolivre.com.br/%s";

    private final AffiliateProperties properties;

    public MercadoLivreAffiliateConnector(AffiliateProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateLink(String originalUrl, String productId, String campaignId) {
        String toolId = properties.getMercadolivre().getToolId();
        if (toolId == null || toolId.isBlank()) {
            log.warn("Mercado Livre toolId not configured, returning direct URL");
            return String.format(BASE_URL, productId);
        }
        return buildUrl(String.format(BASE_URL, productId),
            "matt_tool", toolId,
            "matt_word", campaignId != null ? campaignId : properties.getDefaultCampaign());
    }

    @Override
    public AffiliateNetwork getNetwork() {
        return AffiliateNetwork.MERCADO_LIVRE;
    }

    @Override
    public boolean supports(String storeName) {
        return SUPPORTED_STORES.stream().anyMatch(s -> storeName.contains(s));
    }
}
