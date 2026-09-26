package com.atlasdeals.aiengine.application.agent;

import com.atlasdeals.aiengine.application.port.AiService;
import com.atlasdeals.aiengine.domain.AgentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class ContentAgent implements AiAgent {

    private static final Logger log = LoggerFactory.getLogger(ContentAgent.class);

    private final AiService aiService;

    public ContentAgent(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public AgentType getAgentType() {
        return AgentType.COLLECTOR_AGENT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T execute(Map<String, Object> context, Class<T> responseType) {
        String title = (String) context.get("title");
        String category = (String) context.get("category");
        String storeName = (String) context.get("storeName");
        BigDecimal discountPercentage = (BigDecimal) context.get("discountPercentage");
        String description = (String) context.get("description");
        String brand = (String) context.getOrDefault("brand", "");
        String model = (String) context.getOrDefault("model", "");
        String imageUrl = (String) context.get("imageUrl");

        log.info("ContentAgent executing for product: {}", title);

        String opportunityLevel = determineOpportunityLevel(discountPercentage);
        String generatedTitle = generateTitle(title, brand, model);
        String generatedDescription = generateDescription(title, brand, model, description, storeName, discountPercentage);
        String generatedSummary = generateSummary(title, brand, model, discountPercentage);
        String seoDescription = generateSeoDescription(title, brand, model, discountPercentage);
        String hashtags = generateHashtags(category, brand, title);
        String classifiedCategory = classifyCategory(category);
        String tags = generateTags(category, brand, model);
        String technicalSummary = generateTechnicalSummary(title, brand, model, description);
        String pros = generatePros(category, discountPercentage);
        String cons = generateCons(category);
        String recommendedProfile = generateRecommendedProfile(category);

        if (aiService.isAvailable()) {
            try {
                log.info("AI service available, generating enhanced content for: {}", title);
                String systemPrompt = buildSystemPrompt();
                String userPrompt = buildUserPrompt(title, brand, model, description, storeName, discountPercentage, category);
                String aiResponse = aiService.generate(systemPrompt, userPrompt);
                return (T) parseAiResponse(aiResponse, title, brand, model, description, storeName, category, discountPercentage);
            } catch (Exception e) {
                log.warn("AI service failed, falling back to templates: {}", e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("title", generatedTitle);
        result.put("description", generatedDescription);
        result.put("summary", generatedSummary);
        result.put("seoDescription", seoDescription);
        result.put("hashtags", hashtags);
        result.put("category", classifiedCategory);
        result.put("tags", tags);
        result.put("opportunityLevel", opportunityLevel);
        result.put("technicalSummary", technicalSummary);
        result.put("pros", pros);
        result.put("cons", cons);
        result.put("recommendedProfile", recommendedProfile);

        return (T) result;
    }

    private String determineOpportunityLevel(BigDecimal discountPercentage) {
        if (discountPercentage == null) return "MEDIUM";
        double discount = discountPercentage.doubleValue();
        if (discount >= 50) return "HIGH";
        if (discount >= 20) return "MEDIUM";
        return "LOW";
    }

    private String generateTitle(String title, String brand, String model) {
        if (title != null && title.length() > 60) {
            return title.substring(0, 57) + "...";
        }
        return title;
    }

    private String generateDescription(String title, String brand, String model, String description,
                                       String storeName, BigDecimal discountPercentage) {
        StringBuilder sb = new StringBuilder();
        String discountText = discountPercentage != null
            ? String.format(" com incríveis %.0f%% de desconto", discountPercentage.doubleValue())
            : "";
        sb.append(String.format("🔥 Oferta imperdível! %s%s está disponível na %s. ", title, discountText, storeName));
        if (description != null && !description.isBlank()) {
            sb.append(description);
        } else {
            sb.append(String.format("O %s %s chega para revolucionar sua experiência com tecnologia de ponta. ", brand, model));
            sb.append("Com design moderno e funcionalidades avançadas, este produto oferece o melhor custo-benefício do mercado. ");
        }
        sb.append(String.format("Corra e garanta o seu %s antes que o estoque acabe! Aproveite esta oportunidade única de adquirir um produto de alta qualidade por um preço imbatível.", brand));
        return sb.toString();
    }

    private String generateSummary(String title, String brand, String model, BigDecimal discountPercentage) {
        String discountText = discountPercentage != null
            ? String.format(" - %.0f%% OFF!", discountPercentage.doubleValue())
            : "";
        String summary = String.format("%s%s na %s", title, discountText, "loja");
        return summary.length() > 150 ? summary.substring(0, 147) + "..." : summary;
    }

    private String generateSeoDescription(String title, String brand, String model, BigDecimal discountPercentage) {
        String discountText = discountPercentage != null
            ? String.format("%.0f%% de desconto", discountPercentage.doubleValue())
            : "preço especial";
        String seo = String.format("Compre %s com %s. Ofertas exclusivas, frete rápido e garantia de qualidade. Aproveite!", title, discountText);
        return seo.length() > 160 ? seo.substring(0, 157) + "..." : seo;
    }

    private String generateHashtags(String category, String brand, String title) {
        String baseHashtags = "#Promocao, #Oferta, #Desconto, #Imperdivel, #CompraAgora";
        if (category != null && !category.isBlank()) {
            baseHashtags += ", #" + category.replaceAll("\\s+", "");
        }
        if (brand != null && !brand.isBlank()) {
            baseHashtags += ", #" + brand.replaceAll("\\s+", "");
        }
        String[] words = title != null ? title.split("\\s+") : new String[0];
        for (String word : words) {
            if (word.length() > 3 && !baseHashtags.contains("#" + word)) {
                baseHashtags += ", #" + word.replaceAll("[^\\w]", "");
            }
        }
        return baseHashtags;
    }

    private String classifyCategory(String category) {
        if (category == null || category.isBlank()) return "Geral";
        return category;
    }

    private String generateTags(String category, String brand, String model) {
        String tag = "[\"promocao\", \"oferta\", \"desconto\"";
        if (category != null) tag += ", \"" + category.toLowerCase() + "\"";
        if (brand != null) tag += ", \"" + brand.toLowerCase() + "\"";
        if (model != null) tag += ", \"" + model.toLowerCase() + "\"";
        tag += "]";
        return tag;
    }

    private String generateTechnicalSummary(String title, String brand, String model, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("- ").append(brand).append(" ").append(model).append("\n");
        sb.append("- ").append(title).append("\n");
        sb.append("- Categoria: Produto eletrônico de alta qualidade\n");
        sb.append("- Garantia do fabricante\n");
        sb.append("- Compatibilidade com padrões nacionais e internacionais\n");
        return sb.toString();
    }

    private String generatePros(String category, BigDecimal discountPercentage) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\"Produto de alta qualidade\"");
        if (discountPercentage != null && discountPercentage.doubleValue() >= 20) {
            sb.append(", \"Desconto imperdível de ").append(String.format("%.0f%%", discountPercentage.doubleValue())).append("\"");
        }
        sb.append(", \"Garantia estendida\"");
        sb.append(", \"Entrega rápida\"");
        sb.append(", \"Melhor custo-benefício do mercado\"]");
        return sb.toString();
    }

    private String generateCons(String category) {
        return "[\"Disponibilidade limitada de estoque\", \"Preço pode variar conforme região\", \"Frete não incluso em algumas regiões\"]";
    }

    private String generateRecommendedProfile(String category) {
        if (category == null) return "Consumidores em geral que buscam qualidade e economia";
        return switch (category.toLowerCase()) {
            case "informática", "computadores", "notebooks" -> "Profissionais de TI, gamers e estudantes que precisam de alto desempenho";
            case "smartphones", "celulares" -> "Usuários que buscam tecnologia móvel de ponta com o melhor preço";
            case "eletrodomésticos" -> "Donas de casa e famílias que querem praticidade e economia no dia a dia";
            case "tv", "televisão", "áudio" -> "Amantes de entretenimento e home theater com orçamento consciente";
            case "moda" -> "Pessoas que buscam estilo e conforto com preços acessíveis";
            case "esporte", "fitness" -> "Atletas e praticantes de atividades físicas que buscam equipamentos de qualidade";
            default -> "Consumidores em geral que buscam qualidade e economia";
        };
    }

    private String buildSystemPrompt() {
        return "Você é um especialista em marketing digital e copywriting para e-commerce brasileiro. "
            + "Gere conteúdo persuasivo e otimizado para SEO em português do Brasil.";
    }

    private String buildUserPrompt(String title, String brand, String model, String description,
                                   String storeName, BigDecimal discountPercentage, String category) {
        return String.format("""
            Gere conteúdo completo para o produto:
            Título: %s
            Marca: %s
            Modelo: %s
            Descrição original: %s
            Loja: %s
            Desconto: %.0f%%
            Categoria: %s

            Inclua: título SEO, descrição (2 parágrafos), resumo (1 linha),
            meta description SEO, hashtags, categoria, nível de oportunidade,
            resumo técnico, prós, contras, perfil recomendado.
            """, title, brand, model, description, storeName,
            discountPercentage != null ? discountPercentage.doubleValue() : 0, category);
    }

    private Map<String, Object> parseAiResponse(String aiResponse, String title, String brand, String model,
                                                 String description, String storeName, String category,
                                                 BigDecimal discountPercentage) {
        Map<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", aiResponse.length() > 500 ? aiResponse.substring(0, 500) : aiResponse);
        result.put("summary", generateSummary(title, brand, model, discountPercentage));
        result.put("seoDescription", generateSeoDescription(title, brand, model, discountPercentage));
        result.put("hashtags", generateHashtags(category, brand, title));
        result.put("category", classifyCategory(category));
        result.put("tags", generateTags(category, brand, model));
        result.put("opportunityLevel", determineOpportunityLevel(discountPercentage));
        result.put("technicalSummary", generateTechnicalSummary(title, brand, model, description));
        result.put("pros", generatePros(category, discountPercentage));
        result.put("cons", generateCons(category));
        result.put("recommendedProfile", generateRecommendedProfile(category));
        return result;
    }
}
