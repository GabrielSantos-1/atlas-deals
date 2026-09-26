package com.atlasdeals.affiliateengine.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class GenerateLinkRequest {

    @NotBlank(message = "productId is required")
    private UUID productId;

    @NotBlank(message = "storeName is required")
    private String storeName;

    @NotBlank(message = "originalUrl is required")
    private String originalUrl;

    private String campaignId;

    public GenerateLinkRequest() {
    }

    public GenerateLinkRequest(UUID productId, String storeName, String originalUrl, String campaignId) {
        this.productId = productId;
        this.storeName = storeName;
        this.originalUrl = originalUrl;
        this.campaignId = campaignId;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
}
