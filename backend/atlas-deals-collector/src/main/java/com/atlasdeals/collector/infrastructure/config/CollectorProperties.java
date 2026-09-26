package com.atlasdeals.collector.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "collector")
public class CollectorProperties {

    private String schedule = "0 */15 * * * *";
    private Connectors connectors = new Connectors();

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Connectors getConnectors() {
        return connectors;
    }

    public void setConnectors(Connectors connectors) {
        this.connectors = connectors;
    }

    public static class Connectors {
        private Shopee shopee = new Shopee();
        private Amazon amazon = new Amazon();
        private Kabum kabum = new Kabum();
        private MercadoLivre mercadolivre = new MercadoLivre();

        public Shopee getShopee() { return shopee; }
        public void setShopee(Shopee shopee) { this.shopee = shopee; }
        public Amazon getAmazon() { return amazon; }
        public void setAmazon(Amazon amazon) { this.amazon = amazon; }
        public Kabum getKabum() { return kabum; }
        public void setKabum(Kabum kabum) { this.kabum = kabum; }
        public MercadoLivre getMercadolivre() { return mercadolivre; }
        public void setMercadolivre(MercadoLivre mercadolivre) { this.mercadolivre = mercadolivre; }
    }

    public static class Shopee {
        private boolean enabled = true;
        private String partnerId = "";
        private String affiliateKey = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPartnerId() { return partnerId; }
        public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
        public String getAffiliateKey() { return affiliateKey; }
        public void setAffiliateKey(String affiliateKey) { this.affiliateKey = affiliateKey; }
    }

    public static class Amazon {
        private boolean enabled = true;
        private String accessKey = "";
        private String secretKey = "";
        private String partnerTag = "atlasdeals-20";
        private String marketplace = "www.amazon.com.br";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getPartnerTag() { return partnerTag; }
        public void setPartnerTag(String partnerTag) { this.partnerTag = partnerTag; }
        public String getMarketplace() { return marketplace; }
        public void setMarketplace(String marketplace) { this.marketplace = marketplace; }
    }

    public static class Kabum {
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class MercadoLivre {
        private boolean enabled = true;
        private String clientId = "";
        private String clientSecret = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    }
}
