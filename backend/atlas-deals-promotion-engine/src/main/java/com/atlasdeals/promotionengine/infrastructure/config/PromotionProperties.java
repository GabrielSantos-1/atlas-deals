package com.atlasdeals.promotionengine.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "promotion")
public class PromotionProperties {

    private int minDiscountPercent = 5;
    private int scoreThreshold = 30;
    private int expirationDays = 7;
    private int maxResultsPerPage = 50;
    private Map<String, RuleConfig> rules = new HashMap<>();

    public int getMinDiscountPercent() { return minDiscountPercent; }
    public void setMinDiscountPercent(int minDiscountPercent) { this.minDiscountPercent = minDiscountPercent; }
    public int getScoreThreshold() { return scoreThreshold; }
    public void setScoreThreshold(int scoreThreshold) { this.scoreThreshold = scoreThreshold; }
    public int getExpirationDays() { return expirationDays; }
    public void setExpirationDays(int expirationDays) { this.expirationDays = expirationDays; }
    public int getMaxResultsPerPage() { return maxResultsPerPage; }
    public void setMaxResultsPerPage(int maxResultsPerPage) { this.maxResultsPerPage = maxResultsPerPage; }
    public Map<String, RuleConfig> getRules() { return rules; }
    public void setRules(Map<String, RuleConfig> rules) { this.rules = rules; }

    public static class RuleConfig {
        private boolean enabled = true;
        private double weight = 1.0;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
    }
}
