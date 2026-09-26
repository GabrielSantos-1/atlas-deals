package com.atlasdeals.aiengine.application.port;

public interface AiService {

    String generate(String prompt);

    String generate(String systemPrompt, String userMessage);

    boolean isAvailable();
}
