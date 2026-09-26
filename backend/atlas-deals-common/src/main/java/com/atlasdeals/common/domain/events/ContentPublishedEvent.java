package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.ProductId;
import java.time.Instant;
import java.util.Map;

public class ContentPublishedEvent extends BaseDomainEvent {
    private final ProductId productId;
    private final String platform;
    private final String platformPostId;
    private final String status;
    private final String content;
    private final Map<String, Object> metadata;
    private final Instant publishedAt;

    public ContentPublishedEvent(ProductId productId, String platform, String platformPostId,
                                 String status, String content, Map<String, Object> metadata,
                                 Instant publishedAt) {
        this.productId = productId;
        this.platform = platform;
        this.platformPostId = platformPostId;
        this.status = status;
        this.content = content;
        this.metadata = metadata;
        this.publishedAt = publishedAt;
    }

    @Override
    public String getEventType() { return "content.published"; }

    public ProductId getProductId() { return productId; }
    public String getPlatform() { return platform; }
    public String getPlatformPostId() { return platformPostId; }
    public String getStatus() { return status; }
    public String getContent() { return content; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Instant getPublishedAt() { return publishedAt; }
}
