package com.atlasdeals.imagegenerator.application.port;

import com.atlasdeals.imagegenerator.domain.model.PlatformImageSpec;
import com.atlasdeals.imagegenerator.domain.model.ProductImage;
import com.atlasdeals.imagegenerator.domain.model.PromotionalImage;
import java.util.Map;

public interface ImageProcessor {
    PromotionalImage generatePromotionalImage(ProductImage original, PlatformImageSpec spec, Map<String, String> overlayData);
    PromotionalImage resize(ProductImage image, int width, int height);
    PromotionalImage addOverlay(ProductImage image, Map<String, String> textElements);
    boolean supportsFormat(String format);
}
