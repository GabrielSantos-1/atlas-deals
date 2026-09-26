package com.atlasdeals.aiengine.infrastructure.web;

import com.atlasdeals.aiengine.application.agent.SessionManager;
import com.atlasdeals.aiengine.application.service.ContentGenerationService;
import com.atlasdeals.aiengine.domain.AgentType;
import com.atlasdeals.aiengine.domain.model.AiContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final ContentGenerationService contentGenerationService;
    private final SessionManager sessionManager;

    public AiController(ContentGenerationService contentGenerationService,
                        SessionManager sessionManager) {
        this.contentGenerationService = contentGenerationService;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/generate/{productId}")
    public ResponseEntity<Map<String, Object>> generateContent(@PathVariable UUID productId,
                                                                @RequestBody(required = false) Map<String, Object> params) {
        log.info("Manual content generation requested for product: {}", productId);

        try {
            String title = params != null ? (String) params.getOrDefault("title", "Produto") : "Produto";
            String brand = params != null ? (String) params.getOrDefault("brand", "") : "";
            String model = params != null ? (String) params.getOrDefault("model", "") : "";
            String description = params != null ? (String) params.getOrDefault("description", "") : "";
            String category = params != null ? (String) params.getOrDefault("category", "Geral") : "Geral";
            String storeName = params != null ? (String) params.getOrDefault("storeName", "Loja") : "Loja";
            double discount = params != null ? ((Number) params.getOrDefault("discountPercentage", 0)).doubleValue() : 0;

            AiContent content = contentGenerationService.generateForProduct(
                productId, title, brand, model, description, category, storeName, discount
            );

            Map<String, Object> response = buildContentResponse(content);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate content for product: {}", productId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/content/{productId}")
    public ResponseEntity<Map<String, Object>> getContent(@PathVariable UUID productId) {
        Optional<AiContent> contentOpt = contentGenerationService.getContentByProductId(productId);

        if (contentOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Content not found for product: " + productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        AiContent content = contentOpt.get();
        Map<String, Object> response = buildContentResponse(content);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/platform/{platform}")
    public ResponseEntity<Map<String, Object>> generateForPlatform(@RequestBody Map<String, Object> params,
                                                                    @PathVariable String platform) {
        UUID productId = UUID.fromString((String) params.get("productId"));
        log.info("Platform content generation for product: {}, platform: {}", productId, platform);

        try {
            Map<String, String> platformContent = contentGenerationService.generateForPlatform(productId, platform);
            return ResponseEntity.ok(new HashMap<>(platformContent));
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate platform content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/agents/status")
    public ResponseEntity<Map<String, Object>> getAgentStatuses() {
        Map<String, Object> status = new HashMap<>();
        Map<UUID, Map<String, Object>> sessions = sessionManager.getAllSessions();
        status.put("activeSessions", sessions.size());
        status.put("agentTypes", AgentType.values());
        status.put("sessions", sessions);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/agents/{agentType}/execute")
    public ResponseEntity<Map<String, Object>> executeAgent(@PathVariable String agentType,
                                                             @RequestBody Map<String, Object> context) {
        log.info("Manual agent execution requested: {}", agentType);

        try {
            AgentType type = AgentType.valueOf(agentType.toUpperCase());
            UUID sessionId = UUID.randomUUID();

            Map<String, Object> sessionContext = sessionManager.createSession(sessionId, context);
            Map<String, Object> result = sessionManager.orchestrate(sessionId, sessionContext);

            Map<String, Object> response = new HashMap<>(result);
            response.put("sessionId", sessionId);
            response.put("agentType", type);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid agent type: " + agentType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Agent execution failed: {}", agentType, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Agent execution failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private Map<String, Object> buildContentResponse(AiContent content) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", content.getId());
        response.put("productId", content.getProductId());
        response.put("title", content.getTitle());
        response.put("description", content.getDescription());
        response.put("summary", content.getSummary());
        response.put("seoDescription", content.getSeoDescription());
        response.put("hashtags", content.getHashtags());
        response.put("category", content.getCategory());
        response.put("tags", content.getTags());
        response.put("opportunityLevel", content.getOpportunityLevel());
        response.put("technicalSummary", content.getTechnicalSummary());
        response.put("pros", content.getPros());
        response.put("cons", content.getCons());
        response.put("recommendedProfile", content.getRecommendedProfile());
        response.put("comparisonWithHistory", content.getComparisonWithHistory());
        response.put("comparisonWithCompetitors", content.getComparisonWithCompetitors());
        response.put("contentByPlatform", content.getContentByPlatform());
        response.put("generationStatus", content.getGenerationStatus());
        response.put("aiModel", content.getAiModel());
        response.put("generatedAt", content.getGeneratedAt());
        response.put("createdAt", content.getCreatedAt());
        response.put("updatedAt", content.getUpdatedAt());
        return response;
    }
}
