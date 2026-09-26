package com.atlasdeals.common.application.dto;

import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AnalyticsDTO(
    UUID clickId,
    ProductId productId,
    String platform,
    String source,
    String campaignId,
    String deviceType,
    Money revenue,
    boolean converted,
    Instant eventTime,
    Map<String, String> metadata
) {}
