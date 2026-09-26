package com.atlasdeals.publisher.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "publisher")
public class PublisherConfig {

    private int concurrentPublishers = 3;
    private int maxRetries = 3;
    private int retryBackoffMs = 1000;
    private long queuePollIntervalMs = 30000;
    private long schedulePollIntervalMs = 60000;
    private long retryIntervalMs = 300000;
    private Map<String, PlatformConfig> platforms = new HashMap<>();

    public int getConcurrentPublishers() {
        return concurrentPublishers;
    }

    public void setConcurrentPublishers(int concurrentPublishers) {
        this.concurrentPublishers = concurrentPublishers;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(int retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    public long getQueuePollIntervalMs() {
        return queuePollIntervalMs;
    }

    public void setQueuePollIntervalMs(long queuePollIntervalMs) {
        this.queuePollIntervalMs = queuePollIntervalMs;
    }

    public long getSchedulePollIntervalMs() {
        return schedulePollIntervalMs;
    }

    public void setSchedulePollIntervalMs(long schedulePollIntervalMs) {
        this.schedulePollIntervalMs = schedulePollIntervalMs;
    }

    public long getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(long retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public Map<String, PlatformConfig> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Map<String, PlatformConfig> platforms) {
        this.platforms = platforms;
    }

    public boolean isPlatformEnabled(String platform) {
        PlatformConfig config = platforms.get(platform.toLowerCase());
        return config != null && config.isEnabled();
    }

    public PlatformConfig getPlatform(String platform) {
        return platforms.get(platform.toLowerCase());
    }

    public static class PlatformConfig {
        private boolean enabled = false;
        private String botToken;
        private String chatId;
        private String phoneNumberId;
        private String accessToken;
        private String pageId;
        private String baseUrl;
        private String outputDir;
        private String apiKey;
        private String apiSecret;
        private String accessTokenSecret;
        private String webhookUrl;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public String getPhoneNumberId() {
            return phoneNumberId;
        }

        public void setPhoneNumberId(String phoneNumberId) {
            this.phoneNumberId = phoneNumberId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getPageId() {
            return pageId;
        }

        public void setPageId(String pageId) {
            this.pageId = pageId;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }

        public String getAccessTokenSecret() {
            return accessTokenSecret;
        }

        public void setAccessTokenSecret(String accessTokenSecret) {
            this.accessTokenSecret = accessTokenSecret;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }
    }
}
