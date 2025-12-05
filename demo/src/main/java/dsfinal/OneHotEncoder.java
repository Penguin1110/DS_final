package dsfinal;

import java.util.*;

public class OneHotEncoder {
    // 依據 PDF 定義 vocabulary
    private Set<String> vocabulary;
    private Set<String> stopWords;

    public OneHotEncoder() {
        // 1. 初始化停用詞 (Stop Words)
        stopWords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "in", "of", "on", "to", "with", 
            "is", "are", "it", "this", "that", "for", "by", "at", "be", "have", "has",
            "i", "you", "he", "she", "we", "my", "your", "his", "her", "our",
            "who", "what", "where", "why", "how", "which"
        ));

        // 2. 初始化詞彙表 (Vocabulary)
        // 為了符合化妝品搜尋引擎的特性，我們預先載入美妝相關詞彙
        // 這樣可以確保這些字在處理時被視為"已知詞彙"
        vocabulary = new HashSet<>();
        initializeCosmeticVocabulary();
    }

    /**
     * 預載入化妝品領域詞彙
     * (這些詞彙與 KeywordExtractor 中的分類一致，確保系統知識庫統一)
     */
    private void initializeCosmeticVocabulary() {
        // 功能性關鍵字 (Functional)
        Collections.addAll(vocabulary, 
            "moisturizing", "hydrating",       // 保濕
            "whitening", "brightening",        // 美白
            "anti-aging", "repair",            // 抗老修復
            "waterproof", "oil-control",       // 防水、控油
            "long-lasting", "smudge-proof",    // 持久、抗暈
            "sensitive", "acne",               // 敏感肌、痘痘
            "sunscreen", "uv"                  // 防曬
        );

        // 分類關鍵字 (Category)
        Collections.addAll(vocabulary,
            "foundation", "concealer", "primer", "powder", "cushion", // 底妝
            "lipstick", "lip-gloss", "lip-balm", "tint",              // 唇彩
            "eyeshadow", "mascara", "eyeliner", "eyebrow",            // 眼彩
            "lotion", "cream", "serum", "toner", "cleanser",          // 保養類
            "blush", "highlighter", "contour"                         // 修容
        );
    }

    /**
     * 對外公開的方法，允許動態增加新詞彙
     * 對應 PDF 
     */
    public void addToVocabulary(String word) {
        if (word != null && !word.isEmpty()) {
            vocabulary.add(word.toLowerCase());
        }
    }

    /**
     * 將原始查詢字串轉換為關鍵字列表
     * 對應 PDF encode()
     */
    public List<String> encode(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Tokenize & Normalize
        String[] tokens = query.toLowerCase().split("[\\s\\p{Punct}]+");
        List<String> keywords = new ArrayList<>();
        
        for (String token : tokens) {
            // 2. 過濾空白與停用詞
            if (!token.isEmpty() && !stopWords.contains(token)) {
                // 這裡我們直接回傳清洗後的字
                // 雖然有 vocabulary，但在搜尋引擎中，我們通常允許使用者搜尋 unknown words (例如新品牌名)
                // 所以這裡不做 contains 檢查，vocabulary 主要是作為後續分析或擴充向量使用
                keywords.add(token);
                
                // (可選策略) 如果希望這個系統會"學習"使用者的詞彙，可以在這裡動態加入 vocabulary
                // addToVocabulary(token); 
            }
        }

        return keywords;
    }
}

/*
4. OneHotEncoder（紀）
◎ Variables

List<String> stopWords

Tokenizer tokenizer

◎ Methods
encode(String text)

→ 將英文 query 轉成關鍵字列表

tokenize → 分詞（依空白與符號）

lowercase → 全部變小寫

去掉標點符號

過濾 stopwords（the, with, of…）

移除空字串

回傳 List<String> keywords */