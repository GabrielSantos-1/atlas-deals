package com.atlasdeals.aiengine.infrastructure.ai;

import com.atlasdeals.aiengine.application.port.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@ConditionalOnMissingBean(name = {"openAiService", "anthropicService"})
public class TemplateAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(TemplateAiService.class);

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    private static final String[][] DESCRICOES_ELETRONICOS = {
        {
            "Apresentamos o %s, um produto que combina design sofisticado com tecnologia de ponta. " +
            "Com desempenho excepcional e recursos avançados, ele foi projetado para atender às necessidades " +
            "dos usuários mais exigentes que não abrem mão de qualidade e inovação.",
            "A %s traz o que há de melhor em engenharia e design para oferecer uma experiência única. " +
            "Seja para trabalho ou lazer, este equipamento oferece velocidade, confiabilidade e um acabamento " +
            "premium que faz toda a diferença no dia a dia."
        },
        {
            "O %s chega ao mercado brasileiro para estabelecer um novo padrão de qualidade. " +
            "Com componentes de última geração e um design ergonômico impressionante, " +
            "este produto é a escolha ideal para quem busca o melhor custo-benefício.",
            "Experimente o poder da inovação com o novo %s. Cada detalhe foi pensado para proporcionar " +
            "máxima performance, desde o processador robusto até a tela de alta definição. " +
            "Perfeito para transformar sua rotina digital."
        },
        {
            "Descubra o %s, a mais nova revolução em tecnologia. Com specifications de tirar o fôlego " +
            "e um preço que cabe no seu bolso, este produto é a definição de inovação acessível.",
            "A %s está redefinindo os limites da tecnologia com este lançamento incrível. " +
            "Construído com materiais premium e equipado com o que há de mais moderno, " +
            "ele oferece uma experiência incomparável em sua categoria."
        }
    };

    private static final String[][] RESUMOS = {
        {"%s com desempenho superior e design moderno. A melhor escolha para quem busca qualidade e economia!"},
        {"%s: tecnologia avançada com preço imperdível. Compre agora e transforme sua experiência digital!"},
        {"Oferta única: %s com descontos especiais e condições imperdíveis. Garanta já o seu!"}
    };

    private static final String[] SEO_DESCRIPTIONS = {
        "Compre %s com o melhor preço do mercado. Ofertas exclusivas, frete rápido, garantia de qualidade e pagamento facilitado. Aproveite!",
        "%s em promoção na Atlas Deals. Descontos imperdíveis em produtos originais com nota fiscal e garantia. Clique e confira!",
        "O %s mais barato está aqui. Comparação de preços, avaliações de usuários e ofertas atualizadas diariamente. Economize agora!"
    };

    private static final String[][] CATEGORIAS = {
        "Eletrônicos", "Informática", "Smartphones", "Áudio e Vídeo", "Games",
        "Casa Inteligente", "Acessórios", "Periféricos", "Wearables", "Redes e Conectividade"
    };

    private static final String[] OPORTUNIDADES = {"HIGH", "MEDIUM", "LOW"};

    @Override
    public String generate(String prompt) {
        log.debug("TemplateAiService.generate() called");
        return generateTemplateResponse(prompt);
    }

    @Override
    public String generate(String systemPrompt, String userMessage) {
        log.debug("TemplateAiService.generate(system, user) called");
        return generateTemplateResponse(userMessage);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    private String generateTemplateResponse(String input) {
        String productName = extractProductName(input);
        String brand = extractBrand(input);
        String discountStr = extractDiscount(input);
        double discount = 0;
        try {
            discount = Double.parseDouble(discountStr.replaceAll("[^0-9.]", ""));
        } catch (Exception ignored) {}

        String key = productName + brand + discountStr;
        if (templateCache.containsKey(key)) {
            return templateCache.get(key);
        }

        String response = buildPortugueseContent(productName, brand, discount);
        templateCache.put(key, response);
        return response;
    }

    private String buildPortugueseContent(String productName, String brand, double discount) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int descIdx = random.nextInt(DESCRICOES_ELETRONICOS.length);
        String[] paragraphs = DESCRICOES_ELETRONICOS[descIdx];
        String description = String.format(paragraphs[0], productName, brand)
            + "\n\n"
            + String.format(paragraphs[1], productName, brand);

        int resIdx = random.nextInt(RESUMOS.length);
        String summary = String.format(RESUMOS[resIdx][0], productName);
        if (summary.length() > 150) {
            summary = summary.substring(0, 147) + "...";
        }

        String seo = String.format(SEO_DESCRIPTIONS[random.nextInt(SEO_DESCRIPTIONS.length)], productName);
        if (seo.length() > 160) {
            seo = seo.substring(0, 157) + "...";
        }

        String category = CATEGORIAS[random.nextInt(CATEGORIAS.length)];

        String opportunityLevel;
        if (discount >= 50) {
            opportunityLevel = "HIGH";
        } else if (discount >= 20) {
            opportunityLevel = "MEDIUM";
        } else {
            opportunityLevel = "LOW";
        }

        String hashtags = String.format("#%s #Promocao #Oferta #Desconto #Imperdivel #Qualidade #Tecnologia #CompraSegura #MelhorPreco #AtlasDeals",
            brand.replaceAll("\\s+", ""));

        String title = productName.length() > 60 ? productName.substring(0, 57) + "..." : productName;

        String tags = String.format("[\"%s\", \"%s\", \"promocao\", \"oferta\", \"desconto\", \"eletronicos\"]",
            brand.toLowerCase(), category.toLowerCase());

        String technicalSummary = String.format("- %s\n- Marca: %s\n- Categoria: %s\n- Produto original com garantia\n- Compatibilidade nacional\n- Tecnologia de ponta",
            productName, brand, category);

        String pros = String.format("[\"Produto original com garantia\", \"Desconto especial de %.0f%%\", \"Entrega rápida e segura\", \"Suporte ao cliente dedicado\", \"Melhor custo-beneficio\"]",
            discount);

        String cons = "[\"Estoque limitado\", \"Preco pode variar conforme regiao\", " +
            (discount > 50 ? "\"Promocao por tempo limitado\"" : "\"Frete nao incluso em algumas regioes") + "\"]";

        String recommendedProfile = switch (category) {
            case "Informática" ->
                "Profissionais de TI, gamers e estudantes que precisam de alto desempenho com o melhor preco";
            case "Smartphones" ->
                "Usuarios que buscam tecnologia movel de ponta com custo-beneficio excepcional";
            case "Games" -> "Gamers e entusiastas que querem a melhor experiencia com equipamentos de alto nivel";
            case "Casa Inteligente" ->
                "Pessoas que buscam automatizar suas residencias com tecnologia moderna e acessivel";
            default ->
                "Consumidores em geral que buscam qualidade, garantia e o melhor preco do mercado";
        };

        return String.format("""
            Título: %s
            Descrição: %s
            Resumo: %s
            SEO: %s
            Categoria: %s
            Oportunidade: %s
            Hashtags: %s
            Tags: %s
            Resumo Técnico: %s
            Prós: %s
            Contras: %s
            Perfil: %s
            """, title, description, summary, seo, category, opportunityLevel,
            hashtags, tags, technicalSummary, pros, cons, recommendedProfile);
    }

    private String extractProductName(String input) {
        if (input == null || input.isBlank()) return "Produto";
        String[] lines = input.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("título") || line.toLowerCase().contains("produto")) {
                String val = line.replaceAll("(?i)^.*(título|produto)[:\\s]*", "").trim();
                if (!val.isBlank()) return val;
            }
        }
        String clean = input.replaceAll("(?s).*?(Título|Produto|produto|título)[:\\s]*", "").trim();
        if (clean.length() > 50) clean = clean.substring(0, 50);
        return clean.isBlank() ? "Produto" : clean;
    }

    private String extractBrand(String input) {
        if (input == null) return "";
        String[] lines = input.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("marca")) {
                return line.replaceAll("(?i)^.*marca[:\\s]*", "").trim();
            }
        }
        return "";
    }

    private String extractDiscount(String input) {
        if (input == null) return "0";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)\\s*%").matcher(input);
        return m.find() ? m.group(1) : "0";
    }
}
