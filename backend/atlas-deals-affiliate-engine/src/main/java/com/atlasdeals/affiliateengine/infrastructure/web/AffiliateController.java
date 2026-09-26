package com.atlasdeals.affiliateengine.infrastructure.web;

import com.atlasdeals.affiliateengine.application.dto.AffiliateLinkResponse;
import com.atlasdeals.affiliateengine.application.dto.GenerateLinkRequest;
import com.atlasdeals.affiliateengine.application.dto.RegisterConversionRequest;
import com.atlasdeals.affiliateengine.application.service.AffiliateService;
import com.atlasdeals.affiliateengine.domain.model.AffiliateLink;
import com.atlasdeals.common.domain.AffiliateNetwork;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/affiliate")
public class AffiliateController {

    private static final Logger log = LoggerFactory.getLogger(AffiliateController.class);

    private final AffiliateService affiliateService;

    public AffiliateController(AffiliateService affiliateService) {
        this.affiliateService = affiliateService;
    }

    @PostMapping("/links")
    public ResponseEntity<AffiliateLinkResponse> generateLink(@Valid @RequestBody GenerateLinkRequest request) {
        log.info("Generating affiliate link for product {} on store {}", request.getProductId(), request.getStoreName());
        AffiliateLink link = affiliateService.generateAffiliateLink(
            request.getProductId(),
            request.getStoreName(),
            request.getOriginalUrl(),
            request.getCampaignId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AffiliateLinkResponse(link));
    }

    @GetMapping("/links/{productId}")
    public ResponseEntity<AffiliateLinkResponse> getLinkForProduct(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "AMAZON") AffiliateNetwork network) {
        log.info("Fetching affiliate link for product {} on network {}", productId, network);
        AffiliateLink link = affiliateService.getLinkForProduct(productId, network);
        return ResponseEntity.ok(new AffiliateLinkResponse(link));
    }

    @GetMapping("/links/{productId}/all")
    public ResponseEntity<List<AffiliateLinkResponse>> getAllLinksForProduct(@PathVariable UUID productId) {
        log.info("Fetching all affiliate links for product {}", productId);
        List<AffiliateLink> links = affiliateService.getAllLinksForProduct(productId);
        List<AffiliateLinkResponse> responses = links.stream()
            .map(AffiliateLinkResponse::new)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/links/{linkId}/click")
    public ResponseEntity<AffiliateLinkResponse> registerClick(@PathVariable UUID linkId) {
        log.info("Registering click for link {}", linkId);
        AffiliateLink link = affiliateService.registerClick(linkId);
        return ResponseEntity.ok(new AffiliateLinkResponse(link));
    }

    @PostMapping("/links/{linkId}/conversion")
    public ResponseEntity<AffiliateLinkResponse> registerConversion(
            @PathVariable UUID linkId,
            @Valid @RequestBody RegisterConversionRequest request) {
        log.info("Registering conversion for link {} with revenue {}", linkId, request.getRevenue());
        AffiliateLink link = affiliateService.registerConversion(linkId, request.getRevenue());
        return ResponseEntity.ok(new AffiliateLinkResponse(link));
    }
}
