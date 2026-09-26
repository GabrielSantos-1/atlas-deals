package com.atlasdeals.publisher.application.dto;

import java.time.Instant;

public record PublishResult(
    boolean success,
    String platformPostId,
    String errorMessage,
    Instant publishedAt
) {
}
