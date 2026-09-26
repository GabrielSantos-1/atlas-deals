package com.atlasdeals.imagegenerator.application.port;

import com.atlasdeals.imagegenerator.domain.model.PromotionalImage;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface AiImageGenerator {
    CompletableFuture<PromotionalImage> generatePromotionalImage(
        String productTitle, String storeName, BigDecimal price,
        BigDecimal originalPrice, String platform);
    boolean isAvailable();
}
