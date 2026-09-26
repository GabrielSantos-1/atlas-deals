package com.atlasdeals.aiengine.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String provider = "template";
    private String model = "gpt-4";
    private String apiKey = "";
    private int maxTokens = 2000;
    private double temperature = 0.7;
    private List<String> enabledAgents = new ArrayList<>();

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public List<String> getEnabledAgents() { return enabledAgents; }
    public void setEnabledAgents(List<String> enabledAgents) { this.enabledAgents = enabledAgents; }
}
