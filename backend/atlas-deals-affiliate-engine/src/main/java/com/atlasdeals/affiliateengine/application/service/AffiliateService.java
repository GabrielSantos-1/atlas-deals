package com.atlasdeals.affiliateengine.application.service;

import com.atlasdeals.affiliateengine.application.port.AffiliateLinkGenerator;
import com.atlasdeals.affiliateengine.domain.model.AffiliateLink;
import com.atlasdeals.affiliateengine.domain.repository.AffiliateLinkRepository;
import com.atlasdeals.affiliateengine.infrastructure.config.AffiliateProperties;
import com.atlasdeals.affiliateengine.infrastructure.messaging.AffiliateEventPublisher;
import com.atlasdeals.common.domain.AffiliateNetwork;
import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.events.AffiliateLinkGeneratedEvent;
import com.atlasdeals.common.exception.AtlasDealsNotFoundException;
import com.atlasdeals.common.exception.AtlasDealsValidationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AffiliateService {

    private static final Logger log = LoggerFactory.getLogger(AffiliateService.class);

    private final AffiliateLinkRepository linkRepository;
    private final AffiliateEventPublisher eventPublisher;
    private final AffiliateProperties properties;
    private final List<AffiliateLinkGenerator> generators;

    public AffiliateService(AffiliateLinkRepository linkRepository,
                            AffiliateEventPublisher eventPublisher,
                            AffiliateProperties properties,
                            List<AffiliateLinkGenerator> generators) {
        this.linkRepository = linkRepository;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.generators = generators;
    }

    @Transactional
    public AffiliateLink generateAffiliateLink(UUID productId, String storeName,
                                               String originalUrl, String campaignId) {
        AffiliateLinkGenerator generator = findGenerator(storeName);

        String effectiveCampaign = campaignId != null ? campaignId : properties.getDefaultCampaign();
        String linkId = productId.toString();
        String affiliateUrl = generator.generateLink(originalUrl, linkId, effectiveCampaign);

        if (linkRepository.existsByProductIdAndNetwork(productId, generator.getNetwork())) {
            throw new AtlasDealsValidationException(
                "Affiliate link already exists for product " + productId
                    + " on network " + generator.getNetwork());
        }

        AffiliateLink link = AffiliateLink.builder()
            .productId(productId)
            .storeName(storeName)
            .network(generator.getNetwork())
            .originalUrl(originalUrl)
            .affiliateUrl(affiliateUrl)
            .campaignId(effectiveCampaign)
            .build();

        link = linkRepository.save(link);
        log.info("Generated affiliate link id={} for product {} on {}", link.getId(), productId, generator.getNetwork());

        Map<String, String> trackingParams = Map.of(
            "campaign", effectiveCampaign,
            "network", generator.getNetwork().name()
        );
        eventPublisher.publishAffiliateLinkGenerated(new AffiliateLinkGeneratedEvent(
            ProductId.of(productId), storeName, originalUrl, affiliateUrl,
            generator.getNetwork().name(), effectiveCampaign, trackingParams));

        return link;
    }

    public AffiliateLink getLinkForProduct(UUID productId, AffiliateNetwork network) {
        return linkRepository.findByProductIdAndNetwork(productId, network)
            .orElseThrow(() -> new AtlasDealsNotFoundException(
                "AffiliateLink for product " + productId + " on " + network));
    }

    public List<AffiliateLink> getAllLinksForProduct(UUID productId) {
        return linkRepository.findByProductId(productId);
    }

    @Transactional
    public AffiliateLink registerClick(UUID linkId) {
        AffiliateLink link = linkRepository.findById(linkId)
            .orElseThrow(() -> new AtlasDealsNotFoundException("AffiliateLink", linkId));
        link.registerClick();
        AffiliateLink saved = linkRepository.save(link);
        log.info("Registered click for link {} (product {}), total clicks: {}",
            linkId, link.getProductId(), link.getClickCount());
        return saved;
    }

    @Transactional
    public AffiliateLink registerConversion(UUID linkId, BigDecimal revenue) {
        AffiliateLink link = linkRepository.findById(linkId)
            .orElseThrow(() -> new AtlasDealsNotFoundException("AffiliateLink", linkId));
        BigDecimal effectiveRevenue = revenue != null ? revenue : BigDecimal.ZERO;
        link.registerConversion(effectiveRevenue);
        AffiliateLink saved = linkRepository.save(link);
        log.info("Registered conversion for link {} (product {}), revenue: {}, total conversions: {}",
            linkId, link.getProductId(), effectiveRevenue, link.getConversionCount());
        return saved;
    }

    private AffiliateLinkGenerator findGenerator(String storeName) {
        String normalized = storeName.trim().toLowerCase();
        return generators.stream()
            .filter(g -> g.supports(normalized))
            .findFirst()
            .orElseThrow(() -> new AtlasDealsValidationException(
                "No affiliate generator supports store: " + storeName));
    }
}
