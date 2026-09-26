package com.atlasdeals.affiliateengine.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "affiliate")
public class AffiliateProperties {

    private String defaultCampaign = "atlas-deals";
    private ShopeeConfig shopee = new ShopeeConfig();
    private AmazonConfig amazon = new AmazonConfig();
    private MercadoLivreConfig mercadolivre = new MercadoLivreConfig();
    private KabumConfig kabum = new KabumConfig();
    private AliexpressConfig aliexpress = new AliexpressConfig();

    public String getDefaultCampaign() { return defaultCampaign; }
    public void setDefaultCampaign(String defaultCampaign) { this.defaultCampaign = defaultCampaign; }
    public ShopeeConfig getShopee() { return shopee; }
    public void setShopee(ShopeeConfig shopee) { this.shopee = shopee; }
    public AmazonConfig getAmazon() { return amazon; }
    public void setAmazon(AmazonConfig amazon) { this.amazon = amazon; }
    public MercadoLivreConfig getMercadolivre() { return mercadolivre; }
    public void setMercadolivre(MercadoLivreConfig mercadolivre) { this.mercadolivre = mercadolivre; }
    public KabumConfig getKabum() { return kabum; }
    public void setKabum(KabumConfig kabum) { this.kabum = kabum; }
    public AliexpressConfig getAliexpress() { return aliexpress; }
    public void setAliexpress(AliexpressConfig aliexpress) { this.aliexpress = aliexpress; }

    public static class ShopeeConfig {
        private String partnerId = "";

        public String getPartnerId() { return partnerId; }
        public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    }

    public static class AmazonConfig {
        private String partnerTag = "atlasdeals-20";
        private String accessKey = "";
        private String secretKey = "";

        public String getPartnerTag() { return partnerTag; }
        public void setPartnerTag(String partnerTag) { this.partnerTag = partnerTag; }
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    public static class MercadoLivreConfig {
        private String toolId = "";

        public String getToolId() { return toolId; }
        public void setToolId(String toolId) { this.toolId = toolId; }
    }

    public static class KabumConfig {
        private String partnerId = "";

        public String getPartnerId() { return partnerId; }
        public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    }

    public static class AliexpressConfig {
        private String affiliateTag = "";

        public String getAffiliateTag() { return affiliateTag; }
        public void setAffiliateTag(String affiliateTag) { this.affiliateTag = affiliateTag; }
    }
}
