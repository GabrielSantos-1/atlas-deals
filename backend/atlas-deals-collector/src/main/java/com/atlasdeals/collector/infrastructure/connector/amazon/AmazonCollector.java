package com.atlasdeals.collector.infrastructure.connector.amazon;

import com.atlasdeals.collector.application.dto.CollectionResult;
import com.atlasdeals.collector.application.dto.ConnectorConfig;
import com.atlasdeals.collector.domain.model.RawProduct;
import com.atlasdeals.collector.domain.repository.RawProductRepository;
import com.atlasdeals.collector.infrastructure.config.CollectorProperties;
import com.atlasdeals.collector.infrastructure.connector.BaseCollector;
import com.atlasdeals.collector.infrastructure.messaging.ProductEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AmazonCollector extends BaseCollector {

    private final String accessKey;
    private final String secretKey;
    private final String partnerTag;
    private final String marketplace;
    private final boolean enabled;
    private final HttpClient httpClient;
    private static final String API_ENDPOINT = "webservices.amazon.com.br";
    private static final String API_PATH = "/paapi5/searchitems";
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    public AmazonCollector(CollectorProperties properties,
                           ObjectMapper objectMapper,
                           RawProductRepository repository,
                           ProductEventPublisher eventPublisher) {
        super("Amazon", MAX_REQUESTS_PER_MINUTE, objectMapper, repository, eventPublisher);
        this.accessKey = properties.getConnectors().getAmazon().getAccessKey();
        this.secretKey = properties.getConnectors().getAmazon().getSecretKey();
        this.partnerTag = properties.getConnectors().getAmazon().getPartnerTag();
        this.marketplace = properties.getConnectors().getAmazon().getMarketplace();
        this.enabled = properties.getConnectors().getAmazon().isEnabled();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public ConnectorConfig getConfig() {
        return new ConnectorConfig("Amazon", "https://" + API_ENDPOINT, accessKey, MAX_REQUESTS_PER_MINUTE, enabled);
    }

    @Override
    protected CollectionResult doCollect() {
        log.info("Starting Amazon collection (partnerTag={})", partnerTag.isEmpty() ? "not-configured" : partnerTag);
        Instant start = Instant.now();
        int itemsNew = 0;
        int itemsUpdated = 0;
        int errors = 0;

        try {
            List<String> keywords = List.of("smartphone", "laptop", "headphone", "monitor", "tablet",
                "ssd", "mouse", "teclado", "fone", "cadeira");
            int totalPages = 2;

            for (int page = 1; page <= totalPages; page++) {
                for (String keyword : keywords) {
                    try {
                        List<RawProduct> products = searchItems(keyword, page);
                        for (RawProduct product : products) {
                            if (isDuplicate(product)) {
                                itemsUpdated++;
                            } else {
                                saveProduct(product);
                                itemsNew++;
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error searching Amazon for '{}' page {}: {}", keyword, page, e.getMessage());
                        errors++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Amazon collection failed: {}", e.getMessage(), e);
            return CollectionResult.failure("Amazon", errors + 1, Duration.between(start, Instant.now()));
        }

        return CollectionResult.success("Amazon", itemsNew, itemsUpdated, Duration.between(start, Instant.now()));
    }

    private List<RawProduct> searchItems(String keyword, int page) {
        if (!accessKey.isEmpty() && !secretKey.isEmpty() && !partnerTag.isEmpty()) {
            return searchViaApi(keyword, page);
        }
        return searchMocked(keyword, page);
    }

    private List<RawProduct> searchViaApi(String keyword, int page) {
        List<RawProduct> products = new ArrayList<>();
        try {
            String payload = """
                {
                    "Keywords": "%s",
                    "PartnerTag": "%s",
                    "PartnerType": "Associates",
                    "Marketplace": "www.amazon.com.br",
                    "ItemCount": 10,
                    "ItemPage": %d,
                    "Resources": ["Images.Primary.Large", "ItemInfo.Title", "ItemInfo.Features",
                        "Offers.Listings.Price", "ItemInfo.ByLineInfo", "ItemInfo.Classifications"]
                }
                """.formatted(keyword, partnerTag, page);

            String amzDate = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            String datestamp = amzDate.substring(0, 8);

            String canonicalRequest = "POST\n" + API_PATH + "\n\ncontent-type:application/json;charset=UTF-8\nhost:" + API_ENDPOINT + "\nx-amz-date:" + amzDate + "\n\ncontent-type;host;x-amz-date\n" + sha256Hex(payload);
            String stringToSign = "AWS4-HMAC-SHA256\n" + amzDate + "\n" + datestamp + "/" + "us-east-1" + "/ProductAdvertisingAPI/aws4_request\n" + sha256Hex(canonicalRequest);
            String signature = hmacSha256Hex(hmacSha256(hmacSha256(hmacSha256(hmacSha256(("AWS4" + secretKey).getBytes(), datestamp.getBytes()), "us-east-1".getBytes()), "ProductAdvertisingAPI".getBytes()), "aws4_request".getBytes()), stringToSign.getBytes());

            String authorization = "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + datestamp + "/us-east-1/ProductAdvertisingAPI/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + API_ENDPOINT + API_PATH))
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("X-Amz-Date", amzDate)
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .timeout(Duration.ofSeconds(20))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode items = root.path("SearchResult").path("Items");
                for (JsonNode item : items) {
                    try {
                        String asin = item.path("ASIN").asText();
                        JsonNode title = item.path("ItemInfo").path("Title").path("DisplayValue");
                        JsonNode price = item.path("Offers").path("Listings").get(0).path("Price").path("Amount");
                        JsonNode image = item.path("Images").path("Primary").path("Large").path("URL");
                        JsonNode brand = item.path("ItemInfo").path("ByLineInfo").path("Brand").path("DisplayValue");
                        JsonNode category = item.path("ItemInfo").path("Classifications").path("ProductGroup").path("DisplayValue");

                        String imageUrl = image.isMissingNode() ? "" : image.asText();
                        String brandStr = brand.isMissingNode() ? "" : brand.asText();
                        String categoryStr = category.isMissingNode() ? "Eletrônicos" : category.asText();
                        BigDecimal priceVal = price.isMissingNode() ? BigDecimal.ZERO : BigDecimal.valueOf(price.asDouble());

                        if (!asin.isEmpty() && priceVal.compareTo(BigDecimal.ZERO) > 0) {
                            products.add(new RawProduct(
                                UUID.randomUUID(), "AMZ-" + asin, "Amazon", marketplace,
                                title.asText(), "",
                                priceVal, BigDecimal.ZERO, "BRL",
                                "https://www.amazon.com.br/dp/" + asin + "?tag=" + partnerTag,
                                imageUrl,
                                categoryStr, brandStr, "", asin, "",
                                false, true,
                                "{\"source\":\"amazon\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
                                Instant.now()
                            ));
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse Amazon item: {}", e.getMessage());
                    }
                }
            } else {
                log.warn("Amazon API returned {}: {}", response.statusCode(), response.body().substring(0, Math.min(response.body().length(), 500)));
            }
        } catch (Exception e) {
            log.error("Amazon API request failed: {}", e.getMessage());
        }
        return products;
    }

    private List<RawProduct> searchMocked(String keyword, int page) {
        List<RawProduct> products = new ArrayList<>();
        String idBase = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        products.add(new RawProduct(
            UUID.randomUUID(), "B0" + idBase + "X1", "Amazon", marketplace,
            keyword.substring(0, 1).toUpperCase() + keyword.substring(1) + " Samsung Galaxy S24 Ultra 256GB",
            "Smartphone Samsung Galaxy S24 Ultra, 256GB, 12GB RAM, Câmera 200MP, Tela 6.8\", Titanium Preto",
            BigDecimal.valueOf(6999.00), BigDecimal.valueOf(8799.00), "BRL",
            "https://www.amazon.com.br/dp/B0" + idBase + "X1",
            "https://m.media-amazon.com/images/I/" + idBase + "X1.jpg",
            "Eletrônicos", "Samsung", "Galaxy S24 Ultra", "SM-S928B", "8806095308471",
            true, true, "{\"source\":\"amazon\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
            Instant.now()
        ));

        products.add(new RawProduct(
            UUID.randomUUID(), "B0" + idBase + "Y2", "Amazon", marketplace,
            "Notebook " + keyword.substring(0, 1).toUpperCase() + keyword.substring(1) + " Dell Inspiron 16",
            "Notebook Dell Inspiron 16 5630, Intel Core i7-1360P, 16GB RAM, 512GB SSD, Tela 16\" WVA",
            BigDecimal.valueOf(5299.00), BigDecimal.valueOf(6499.00), "BRL",
            "https://www.amazon.com.br/dp/B0" + idBase + "Y2",
            "https://m.media-amazon.com/images/I/" + idBase + "Y2.jpg",
            "Informática", "Dell", "Inspiron 16 5630", "DELL-INS5630", "5397184651721",
            true, true, "{\"source\":\"amazon\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
            Instant.now()
        ));

        return products;
    }

    private String sha256Hex(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(data.getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private String hmacSha256Hex(byte[] key, byte[] data) throws Exception {
        return HexFormat.of().formatHex(hmacSha256(key, data));
    }
}
