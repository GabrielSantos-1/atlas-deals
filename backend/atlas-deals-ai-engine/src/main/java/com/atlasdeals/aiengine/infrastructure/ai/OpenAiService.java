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
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final RestClient restClient;
    private final String model;
    private final int maxTokens;
    private final double temperature;
    private final boolean available;

    public OpenAiService(AiProperties props) {
        String apiKey = props.getApiKey();
        this.model = props.getModel() != null ? props.getModel() : "gpt-4";
        this.maxTokens = props.getMaxTokens() > 0 ? props.getMaxTokens() : 2000;
        this.temperature = props.getTemperature() > 0 ? props.getTemperature() : 0.7;
        this.available = apiKey != null && !apiKey.isBlank();

        if (this.available) {
            this.restClient = RestClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
            log.info("OpenAI service initialized with model: {}", model);
        } else {
            this.restClient = null;
            log.warn("OpenAI API key not configured. Service will be unavailable.");
        }
    }

    @Override
    public String generate(String prompt) {
        return generate("Você é um assistente útil.", prompt);
    }

    @Override
    public String generate(String systemPrompt, String userMessage) {
        if (!available) {
            throw new IllegalStateException("OpenAI service is not available. Configure OPENAI_API_KEY.");
        }

        try {
            Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", maxTokens,
                "temperature", temperature
            );

            Map response = restClient.post()
                .body(request)
                .retrieve()
                .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            log.warn("Unexpected OpenAI response format: {}", response);
            return "";
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI API call failed", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
