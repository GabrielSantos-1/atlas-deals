package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AnalyticsEvent extends BaseDomainEvent {
    private final UUID clickId;
    private final ProductId productId;
    private final String platform;
    private final String source;
    private final String campaignId;
    private final String deviceType;
    private final Money revenue;
    private final boolean converted;
    private final Instant eventTime;
    private final Map<String, String> metadata;

    private AnalyticsEvent(UUID clickId, ProductId productId, String platform, String source,
                           String campaignId, String deviceType, Money revenue, boolean converted,
                           Instant eventTime, Map<String, String> metadata) {
        this.clickId = clickId;
        this.productId = productId;
        this.platform = platform;
        this.source = source;
        this.campaignId = campaignId;
        this.deviceType = deviceType;
        this.revenue = revenue;
        this.converted = converted;
        this.eventTime = eventTime;
        this.metadata = metadata;
    }

    public static AnalyticsEvent click(UUID clickId, ProductId productId, String platform,
                                       String source, String campaignId, String deviceType,
                                       Map<String, String> metadata) {
        return new AnalyticsEvent(clickId, productId, platform, source, campaignId, deviceType,
                                  null, false, Instant.now(), metadata);
    }

    public static AnalyticsEvent conversion(UUID clickId, ProductId productId, String platform,
                                             String source, String campaignId, String deviceType,
                                             Money revenue, Map<String, String> metadata) {
        return new AnalyticsEvent(clickId, productId, platform, source, campaignId, deviceType,
                                  revenue, true, Instant.now(), metadata);
    }

    @Override
    public String getEventType() { return converted ? "analytics.conversion" : "analytics.click"; }

    public UUID getClickId() { return clickId; }
    public ProductId getProductId() { return productId; }
    public String getPlatform() { return platform; }
    public String getSource() { return source; }
    public String getCampaignId() { return campaignId; }
    public String getDeviceType() { return deviceType; }
    public Money getRevenue() { return revenue; }
    public boolean isConverted() { return converted; }
    public Instant getEventTime() { return eventTime; }
    public Map<String, String> getMetadata() { return metadata; }
}
