package com.atlasdeals.affiliateengine.infrastructure.connector.amazon;

import com.atlasdeals.affiliateengine.application.port.AffiliateLinkGenerator;
import com.atlasdeals.affiliateengine.infrastructure.config.AffiliateProperties;
import com.atlasdeals.affiliateengine.infrastructure.connector.BaseAffiliateConnector;
import com.atlasdeals.common.domain.AffiliateNetwork;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AmazonAffiliateConnector extends BaseAffiliateConnector implements AffiliateLinkGenerator {

    private static final Logger log = LoggerFactory.getLogger(AmazonAffiliateConnector.class);
    private static final List<String> SUPPORTED_STORES = List.of("amazon", "amazon br", "amazon brasil", "amazon.com.br");
    private static final Pattern ASIN_PATTERN = Pattern.compile(
        "/(?:dp|product|gp/product|exec/obidos/ASIN)/([A-Z0-9]{10})", Pattern.CASE_INSENSITIVE);
    private static final String AFFILIATE_URL_TEMPLATE = "https://www.amazon.com.br/dp/%s";

    private final AffiliateProperties properties;

    public AmazonAffiliateConnector(AffiliateProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateLink(String originalUrl, String productId, String campaignId) {
        String asin = extractAsin(originalUrl);
        if (asin == null) {
            asin = productId;
            log.warn("Could not extract ASIN from URL {}, using productId {} as ASIN", originalUrl, productId);
        }

        String partnerTag = properties.getAmazon().getPartnerTag();
        if (partnerTag == null || partnerTag.isBlank()) {
            log.warn("Amazon partnerTag not configured, returning direct URL");
            return String.format(AFFILIATE_URL_TEMPLATE, asin);
        }

        return buildUrl(String.format(AFFILIATE_URL_TEMPLATE, asin),
            "tag", partnerTag,
            "ref", "atlas_deals");
    }

    @Override
    public AffiliateNetwork getNetwork() {
        return AffiliateNetwork.AMAZON;
    }

    @Override
    public boolean supports(String storeName) {
        return SUPPORTED_STORES.stream().anyMatch(s -> storeName.contains(s));
    }

    private String extractAsin(String url) {
        if (url == null) return null;
        Matcher matcher = ASIN_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }
}
