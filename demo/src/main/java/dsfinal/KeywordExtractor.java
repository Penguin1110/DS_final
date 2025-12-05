package dsfinal;

import java.util.*;

public class KeywordExtractor {
    private Set<String> stopWords;
    
    // ✅ 新增：化妝品專用詞庫 (對應 PDF 中的分類與功能)
    private Set<String> functionalKeywords; // 功能詞 (e.g., moisturizing)
    private Set<String> categoryKeywords;   // 分類詞 (e.g., foundation)

    public KeywordExtractor() {
        // 1. 初始化通用停用詞
        stopWords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "in", "of", "on", "to", "with", 
            "is", "are", "it", "this", "that", "for", "by", "at", "be", "have", "has"
        ));

        // 2. 初始化功能性關鍵字 (Functional Keywords) - 權重 0.25 
        // 對應 PDF: 保濕補水、美白亮膚、抗老修復、防水、抗暈
        functionalKeywords = new HashSet<>();
        Collections.addAll(functionalKeywords, 
            "moisturizing", "hydrating",       // 保濕
            "whitening", "brightening",        // 美白
            "anti-aging", "repair",            // 抗老修復
            "waterproof", "oil-control",       // 防水、控油
            "long-lasting", "smudge-proof",    // 持久、抗暈
            "sensitive", "acne",               // 敏感肌、痘痘 (膚質需求)
            "sunscreen", "uv"                  // 防曬
        );

        // 3. 初始化分類關鍵字 (Category Keywords) 
        // 對應 PDF: 臉部(粉底,遮瑕...), 唇部(口紅...), 眼部(眼影...)
        categoryKeywords = new HashSet<>();
        Collections.addAll(categoryKeywords,
            "foundation", "concealer", "primer", "powder", "cushion", // 底妝
            "lipstick", "lip-gloss", "lip-balm", "tint",              // 唇彩
            "eyeshadow", "mascara", "eyeliner", "eyebrow",            // 眼彩
            "lotion", "cream", "serum", "toner", "cleanser",          // 保養類
            "blush", "highlighter", "contour"                         // 修容
        );
    }

    /**
     * 提取關鍵字並計算詞頻
     */
    public Map<String, Integer> extractKeywords(String content) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        if (content == null || content.isEmpty()) return frequencyMap;

        String[] tokens = content.toLowerCase().split("[\\s\\p{Punct}]+");

        for (String token : tokens) {
            // 過濾停用詞
            if (token.length() > 1 && !stopWords.contains(token)) {
                frequencyMap.put(token, frequencyMap.getOrDefault(token, 0) + 1);
            }
        }
        return frequencyMap;
    }

    /**
     * 判斷是否為功能性關鍵字 (供 ProductRanker 使用)
     */
    public boolean isFunctionalKeyword(String word) {
        return functionalKeywords.contains(word.toLowerCase());
    }

    /**
     * 判斷是否為分類關鍵字 (供 ProductRanker 使用)
     */
    public boolean isCategoryKeyword(String word) {
        return categoryKeywords.contains(word.toLowerCase());
    }
    
    /**
     * 簡單計算基礎分數 (ProductRanker 會做更複雜的加權)
     */
    public double calculateScore(List<String> queryKeywords, Map<String, Integer> freqMap) {
        double score = 0.0;
        for (String keyword : queryKeywords) {
            if (freqMap.containsKey(keyword)) {
                int count = freqMap.get(keyword);
                
                // 這裡稍微加一點權重邏輯，讓 KeywordExtractor 也能反映重要性
                if (isFunctionalKeyword(keyword)) {
                    score += count * 2.5; // 模擬功能詞的高權重
                } else if (isCategoryKeyword(keyword)) {
                    score += count * 2.0; // 分類詞也很重要
                } else {
                    score += count * 1.0; // 普通詞
                }
            }
        }
        return score;
    }
}

/*
8. KeywordExtractor
◎ Variables

stopWords

Stemmer（可選）

Map<String, Integer> frequencyMap

◎ Methods
extractKeywords(String content)

→ 從內容取出所有詞與出現次數

tokenize

lowercase

過濾停用字

統計詞頻頻率

回傳 Map<keyword, freq>

calculateScore(List<String> queryKeywords, Map<String, Integer> freq)

→ 為每個 keyword 計算與 query 的相關分數

基礎 score = freq * 基本權重

若 keyword 在 query 中 → 額外加分

若 keyword 在 title 出現 → 再加強權重

回傳 final score */