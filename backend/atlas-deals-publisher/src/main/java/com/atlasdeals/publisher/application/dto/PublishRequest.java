package com.atlasdeals.publisher.application.dto;

import java.util.Map;

public record PublishRequest(
    String productId,
    String content,
    String imageUrl,
    String affiliateUrl,
    Map<String, Object> metadata
) {
}
