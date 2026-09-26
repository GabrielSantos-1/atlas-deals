package com.atlasdeals.aiengine.application.service;

import com.atlasdeals.aiengine.application.agent.SessionManager;
import com.atlasdeals.aiengine.domain.GenerationStatus;
import com.atlasdeals.aiengine.domain.model.AiContent;
import com.atlasdeals.aiengine.infrastructure.messaging.AiEventPublisher;
import com.atlasdeals.aiengine.infrastructure.persistence.AiContentRepository;
import com.atlasdeals.common.domain.events.ContentGeneratedEvent;
import com.atlasdeals.common.domain.events.PromotionDetectedEvent;
import com.atlasdeals.common.domain.ProductId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContentGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentGenerationService.class);

    private final AiContentRepository aiContentRepository;
    private final SessionManager sessionManager;
    private final PlatformContentFormatter platformContentFormatter;
    private final AiEventPublisher eventPublisher;

    public ContentGenerationService(AiContentRepository aiContentRepository,
                                    SessionManager sessionManager,
                                    PlatformContentFormatter platformContentFormatter,
                                    AiEventPublisher eventPublisher) {
        this.aiContentRepository = aiContentRepository;
        this.sessionManager = sessionManager;
        this.platformContentFormatter = platformContentFormatter;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AiContent generateForPromotion(PromotionDetectedEvent event) {
        UUID productUuid = event.getProductId().getValue();
        log.info("Starting content generation for product: {}", productUuid);

        Optional<AiContent> existing = aiContentRepository.findByProductId(productUuid);
        if (existing.isPresent() && existing.get().getGenerationStatus() == GenerationStatus.COMPLETED) {
            log.info("Content already generated for product: {}", productUuid);
            return existing.get();
        }

        AiContent content = existing.orElseGet(() -> {
            AiContent c = new AiContent(productUuid);
            c.setGenerationStatus(GenerationStatus.PENDING);
            return aiContentRepository.save(c);
        });

        content.setGenerationStatus(GenerationStatus.PROCESSING);
        content.setAiModel("template");
        aiContentRepository.save(content);

        try {
            Map<String, Object> context = buildContext(event);
            Map<String, Object> result = sessionManager.orchestrate(productUuid, context);

            content.setTitle((String) result.getOrDefault("title", event.getTitle()));
            content.setDescription((String) result.getOrDefault("description", ""));
            content.setSummary((String) result.getOrDefault("summary", ""));
            content.setSeoDescription((String) result.getOrDefault("seoDescription", ""));
            content.setHashtags((String) result.getOrDefault("hashtags", ""));
            content.setCategory((String) result.getOrDefault("category", event.getCategory()));
            content.setTags((String) result.getOrDefault("tags", "[]"));
            content.setOpportunityLevel((String) result.getOrDefault("opportunityLevel", "MEDIUM"));
            content.setTechnicalSummary((String) result.getOrDefault("technicalSummary", ""));
            content.setPros((String) result.getOrDefault("pros", "[]"));
            content.setCons((String) result.getOrDefault("cons", "[]"));
            content.setRecommendedProfile((String) result.getOrDefault("recommendedProfile", ""));
            content.setComparisonWithHistory(generateComparisonWithHistory(event));
            content.setComparisonWithCompetitors(generateComparisonWithCompetitors(event));

            Map<String, String> contentByPlatform = platformContentFormatter.formatForAllPlatforms(content);
            content.setContentByPlatform(contentByPlatform);

            content.setGenerationStatus(GenerationStatus.COMPLETED);
            content.setGeneratedAt(Instant.now());
            content.setTokensUsed(0);
            content.setPromptTokens(0);
            content.setCompletionTokens(0);
            aiContentRepository.save(content);

            publishContentGeneratedEvent(event, content, contentByPlatform);

            log.info("Content generation completed for product: {}", productUuid);
        } catch (Exception e) {
            log.error("Content generation failed for product: {}", productUuid, e);
            content.setGenerationStatus(GenerationStatus.FAILED);
            aiContentRepository.save(content);
        } finally {
            sessionManager.clearSession(productUuid);
        }

        return content;
    }

    @Transactional
    public AiContent generateForProduct(UUID productId, String title, String brand, String model,
                                        String description, String category, String storeName,
                                        double discountPercentage) {
        Map<String, Object> context = new HashMap<>();
        context.put("title", title);
        context.put("brand", brand);
        context.put("model", model);
        context.put("description", description);
        context.put("category", category);
        context.put("storeName", storeName);
        context.put("discountPercentage", java.math.BigDecimal.valueOf(discountPercentage));

        AiContent content = new AiContent(productId);
        content.setGenerationStatus(GenerationStatus.PROCESSING);
        content.setAiModel("template");
        content = aiContentRepository.save(content);

        try {
            Map<String, Object> result = sessionManager.orchestrate(productId, context);

            content.setTitle((String) result.getOrDefault("title", title));
            content.setDescription((String) result.getOrDefault("description", ""));
            content.setSummary((String) result.getOrDefault("summary", ""));
            content.setSeoDescription((String) result.getOrDefault("seoDescription", ""));
            content.setHashtags((String) result.getOrDefault("hashtags", ""));
            content.setCategory((String) result.getOrDefault("category", category));
            content.setTags((String) result.getOrDefault("tags", "[]"));
            content.setOpportunityLevel((String) result.getOrDefault("opportunityLevel", "MEDIUM"));
            content.setTechnicalSummary((String) result.getOrDefault("technicalSummary", ""));
            content.setPros((String) result.getOrDefault("pros", "[]"));
            content.setCons((String) result.getOrDefault("cons", "[]"));
            content.setRecommendedProfile((String) result.getOrDefault("recommendedProfile", ""));

            Map<String, String> contentByPlatform = platformContentFormatter.formatForAllPlatforms(content);
            content.setContentByPlatform(contentByPlatform);

            content.setGenerationStatus(GenerationStatus.COMPLETED);
            content.setGeneratedAt(Instant.now());
            aiContentRepository.save(content);
        } catch (Exception e) {
            log.error("Content generation failed for product: {}", productId, e);
            content.setGenerationStatus(GenerationStatus.FAILED);
            aiContentRepository.save(content);
        }

        return content;
    }

    public Optional<AiContent> getContentByProductId(UUID productId) {
        return aiContentRepository.findByProductId(productId);
    }

    public Optional<AiContent> getContentById(UUID id) {
        return aiContentRepository.findById(id);
    }

    public Map<String, String> generateForPlatform(UUID productId, String platform) {
        Optional<AiContent> contentOpt = aiContentRepository.findByProductId(productId);
        if (contentOpt.isEmpty()) {
            throw new IllegalArgumentException("No content found for product: " + productId);
        }
        AiContent content = contentOpt.get();
        Map<String, String> platformContent = new HashMap<>();
        platformContent.put(platform, platformContentFormatter.formatForPlatform(content, platform));
        return platformContent;
    }

    private Map<String, Object> buildContext(PromotionDetectedEvent event) {
        Map<String, Object> context = new HashMap<>();
        context.put("productId", event.getProductId().getValue());
        context.put("title", event.getTitle());
        context.put("category", event.getCategory());
        context.put("storeName", event.getStoreName());
        context.put("discountPercentage", event.getDiscountPercentage());
        context.put("url", event.getUrl());
        context.put("imageUrl", event.getImageUrl());
        context.put("inStock", event.isInStock());
        context.put("tags", event.getTags());
        context.put("reason", event.getReason());
        if (event.getCurrentPrice() != null) {
            context.put("currentPrice", event.getCurrentPrice().getAmount());
        }
        if (event.getOriginalPrice() != null) {
            context.put("originalPrice", event.getOriginalPrice().getAmount());
        }
        return context;
    }

    private String generateComparisonWithHistory(PromotionDetectedEvent event) {
        if (event.getOriginalPrice() != null && event.getCurrentPrice() != null) {
            java.math.BigDecimal original = event.getOriginalPrice().getAmount();
            java.math.BigDecimal current = event.getCurrentPrice().getAmount();
            java.math.BigDecimal savings = original.subtract(current);
            return String.format(
                "Preço anterior: R$ %.2f | Preço atual: R$ %.2f | Economia de R$ %.2f (%.0f%% de desconto)",
                original, current, savings, event.getDiscountPercentage()
            );
        }
        return "Produto com preço já reduzido. Histórico de preços disponível na plataforma.";
    }

    private String generateComparisonWithCompetitors(PromotionDetectedEvent event) {
        return String.format(
            "O %s está com preço competitivo no mercado. Recomendamos comparar com outras lojas " +
            "para garantir a melhor oferta. A %s oferece garantia e suporte oficial.",
            event.getTitle(), event.getStoreName()
        );
    }

    private void publishContentGeneratedEvent(PromotionDetectedEvent event, AiContent content,
                                               Map<String, String> contentByPlatform) {
        try {
            ContentGeneratedEvent generatedEvent = new ContentGeneratedEvent(
                event.getProductId(),
                contentByPlatform,
                content.getTitle(),
                content.getDescription(),
                content.getSummary(),
                content.getSeoDescription(),
                content.getHashtags(),
                content.getCategory(),
                content.getTags(),
                content.getOpportunityLevel(),
                content.getTechnicalSummary(),
                content.getPros(),
                content.getCons(),
                content.getRecommendedProfile(),
                event.getImageUrl(),
                event.getStoreName(),
                event.getCurrentPrice() != null ? event.getCurrentPrice().getAmount() : null,
                event.getOriginalPrice() != null ? event.getOriginalPrice().getAmount() : null
            );
            eventPublisher.publishContentGenerated(generatedEvent);
        } catch (Exception e) {
            log.error("Failed to publish ContentGeneratedEvent for product: {}", event.getProductId(), e);
        }
    }
}
