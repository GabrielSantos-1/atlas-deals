package com.atlasdeals.aiengine.infrastructure.ai;

import com.atlasdeals.aiengine.application.port.AiService;
import com.atlasdeals.aiengine.infrastructure.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "anthropic")
public class AnthropicService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AnthropicService.class);
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestClient restClient;
    private final String model;
    private final int maxTokens;
    private final boolean available;

    public AnthropicService(AiProperties props) {
        String apiKey = props.getApiKey();
        this.model = props.getModel() != null ? props.getModel() : "claude-3-opus-20240229";
        this.maxTokens = props.getMaxTokens() > 0 ? props.getMaxTokens() : 2000;
        this.available = apiKey != null && !apiKey.isBlank();

        if (this.available) {
            this.restClient = RestClient.builder()
                .baseUrl(ANTHROPIC_API_URL)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
                .defaultHeader("Content-Type", "application/json")
                .build();
            log.info("Anthropic service initialized with model: {}", model);
        } else {
            this.restClient = null;
            log.warn("Anthropic API key not configured. Service will be unavailable.");
        }
    }

    @Override
    public String generate(String prompt) {
        return generate("Você é um assistente útil.", prompt);
    }

    @Override
    public String generate(String systemPrompt, String userMessage) {
        if (!available) {
            throw new IllegalStateException("Anthropic service is not available. Configure ANTHROPIC_API_KEY.");
        }

        try {
            Map<String, Object> request = Map.of(
                "model", model,
                "system", systemPrompt,
                "messages", List.of(
                    Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", maxTokens
            );

            Map response = restClient.post()
                .body(request)
                .retrieve()
                .body(Map.class);

            if (response != null && response.containsKey("content")) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                if (!content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }

            log.warn("Unexpected Anthropic response format: {}", response);
            return "";
        } catch (Exception e) {
            log.error("Anthropic API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Anthropic API call failed", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
