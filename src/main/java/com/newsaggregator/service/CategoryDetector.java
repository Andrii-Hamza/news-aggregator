package com.newsaggregator.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CategoryDetector {

    // Ordered map: first match wins. More specific categories first.
    private static final Map<String, String[]> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYWORDS.put("Gold", new String[]{
                "gold", "gold price", "bullion", "precious metal"
        });
        CATEGORY_KEYWORDS.put("Oil", new String[]{
                "oil", "crude", "petroleum", "opec", "brent", "barrel"
        });
        CATEGORY_KEYWORDS.put("Crypto", new String[]{
                "bitcoin", "crypto", "ethereum", "blockchain", "btc"
        });
        CATEGORY_KEYWORDS.put("Wars", new String[]{
                "war", "military", "troops", "missile", "airstrike", "bombing",
                "invasion", "conflict", "combat", "artillery", "drone strike",
                "ceasefire", "frontline", "battlefield"
        });
        CATEGORY_KEYWORDS.put("Defense", new String[]{
                "nato", "defense", "defence", "arms deal", "weapons", "pentagon",
                "army", "navy", "air force"
        });
        CATEGORY_KEYWORDS.put("Sanctions", new String[]{
                "sanction", "embargo", "trade ban", "tariff"
        });
        CATEGORY_KEYWORDS.put("Elections", new String[]{
                "election", "vote", "ballot", "polling", "campaign",
                "candidate", "primary", "referendum"
        });
        CATEGORY_KEYWORDS.put("Diplomacy", new String[]{
                "diplomat", "summit", "treaty", "ambassador", "negotiations",
                "bilateral", "united nations", "g7", "g20"
        });
        CATEGORY_KEYWORDS.put("Economy", new String[]{
                "economy", "economic", "gdp", "inflation", "interest rate",
                "federal reserve", "central bank", "recession", "stock market",
                "wall street", "dow jones", "nasdaq", "s&p", "fiscal",
                "monetary", "trade", "export", "import", "market", "bank",
                "finance", "financial", "investor", "investment"
        });
        CATEGORY_KEYWORDS.put("Politics", new String[]{
                "president", "congress", "parliament", "senate", "minister",
                "government", "policy", "legislation", "law", "political",
                "democrat", "republican", "opposition", "ruling party"
        });
        CATEGORY_KEYWORDS.put("World", new String[]{
                "international", "global", "world", "country", "nation",
                "foreign", "abroad"
        });
    }

    /**
     * Detect category from the article title.
     * Returns the first matching category, or "World" as default.
     */
    public String detectCategory(String title) {
        if (title == null || title.isBlank()) {
            return "World";
        }

        String lowerTitle = title.toLowerCase();

        for (Map.Entry<String, String[]> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerTitle.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return "World";
    }
}
