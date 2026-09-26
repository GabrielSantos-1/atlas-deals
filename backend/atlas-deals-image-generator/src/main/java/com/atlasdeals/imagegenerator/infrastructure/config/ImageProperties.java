package com.atlasdeals.imagegenerator.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;

@ConfigurationProperties(prefix = "image")
public class ImageProperties {

    private final Storage storage;
    private final Download download;
    private final Processing processing;
    private final Ai ai;

    public ImageProperties(Storage storage, Download download, Processing processing, Ai ai) {
        this.storage = storage;
        this.download = download;
        this.processing = processing;
        this.ai = ai;
    }

    public Storage getStorage() { return storage; }
    public Download getDownload() { return download; }
    public Processing getProcessing() { return processing; }
    public Ai getAi() { return ai; }

    public static class Storage {
        private String path = "./data/images";
        private long maxSize = 1073741824L;

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public long getMaxSize() { return maxSize; }
        public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
    }

    public static class Download {
        private int timeout = 30000;
        private int maxRetries = 3;
        private String userAgent = "AtlasDeals/1.0";

        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    }

    public static class Processing {
        private double quality = 0.85;
        private int maxDimension = 4096;

        public double getQuality() { return quality; }
        public void setQuality(double quality) { this.quality = quality; }
        public int getMaxDimension() { return maxDimension; }
        public void setMaxDimension(int maxDimension) { this.maxDimension = maxDimension; }
    }

    public static class Ai {
        private boolean enabled = false;
        private String provider = "openai";
        private OpenAi openai = new OpenAi();
        private Stability stability = new Stability();

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public OpenAi getOpenai() { return openai; }
        public void setOpenai(OpenAi openai) { this.openai = openai; }
        public Stability getStability() { return stability; }
        public void setStability(Stability stability) { this.stability = stability; }

        public static class OpenAi {
            private String apiKey = "";
            private String model = "dall-e-3";
            private String quality = "standard";
            private String size = "1792x1024";

            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }
            public String getModel() { return model; }
            public void setModel(String model) { this.model = model; }
            public String getQuality() { return quality; }
            public void setQuality(String quality) { this.quality = quality; }
            public String getSize() { return size; }
            public void setSize(String size) { this.size = size; }
        }

        public static class Stability {
            private String apiKey = "";
            private String engine = "stable-diffusion-xl-1024-v1-0";

            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }
            public String getEngine() { return engine; }
            public void setEngine(String engine) { this.engine = engine; }
        }
    }
}
