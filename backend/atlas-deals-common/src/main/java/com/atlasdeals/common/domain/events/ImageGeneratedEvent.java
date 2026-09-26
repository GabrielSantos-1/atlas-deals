package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.ProductId;

public class ImageGeneratedEvent extends BaseDomainEvent {

    private final ProductId productId;
    private final String platform;
    private final String localPath;
    private final int width;
    private final int height;
    private final String format;

    public ImageGeneratedEvent(ProductId productId, String platform, String localPath,
                                int width, int height, String format) {
        this.productId = productId;
        this.platform = platform;
        this.localPath = localPath;
        this.width = width;
        this.height = height;
        this.format = format;
    }

    @Override
    public String getEventType() {
        return "image.generated";
    }

    public ProductId getProductId() { return productId; }
    public String getPlatform() { return platform; }
    public String getLocalPath() { return localPath; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getFormat() { return format; }
}
