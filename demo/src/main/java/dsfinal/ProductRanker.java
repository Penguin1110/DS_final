package dsfinal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProductRanker {
    // 依據 PDF  硬性規定的權重
    // 這些是專家知識，寫死不變，確保結果符合化妝品搜尋的邏輯
    private static final double WEIGHT_TITLE = 0.7;       // T: 主標題權重
    private static final double WEIGHT_FUNCTIONAL = 0.25; // F: 功能性關鍵字權重 (如保濕、美白)
    private static final double WEIGHT_OTHER = 0.05;      // O: 其他/內文關鍵字權重
    private static final double WEIGHT_CHILD_PAGE = 0.3;  // 子頁面分數傳遞權重 [cite: 40]

    private KeywordExtractor extractor;

    public ProductRanker() {
        // Ranker 需要依賴 Extractor 來判斷哪些字是"功能性"的
        this.extractor = new KeywordExtractor();
    }

    /**
     * 對搜尋結果進行排序
     * @param pages 抓取下來的頁面列表
     * @param queryKeywords 使用者的搜尋關鍵字 (已轉成 List)
     * @return 排序後的頁面列表 (分數高 -> 低)
     */
    public List<PageResult> rankResults(List<PageResult> pages, List<String> queryKeywords) {
        // 1. 計算每一頁的分數
        for (PageResult page : pages) {
            page.score = calculatePageScore(page, queryKeywords);
        }

        // 2. 排序 (Sort) - 分數高的排前面
        // 使用 Lambda 表達式：比較 p2.score vs p1.score (降冪排序)
        Collections.sort(pages, (p1, p2) -> Double.compare(p2.score, p1.score));

        return pages;
    }

    /**
     * 核心評分公式：FinalScore = (0.7T + 0.25F + 0.05O) + 0.3 * ChildScore
     * 依據 PDF 
     */
    private double calculatePageScore(PageResult page, List<String> queryKeywords) {
        if (page == null || queryKeywords == null || queryKeywords.isEmpty()) {
            return 0.0;
        }

        // --- 第一部分：內容相關性 (Content Score) ---
        double contentScore = 0.0;

        // 預先統計內文詞頻 (Map<Keyword, Frequency>)
        Map<String, Integer> contentFreq = extractor.extractKeywords(page.content);
        // 標題轉小寫以利比對
        String titleLower = (page.title != null) ? page.title.toLowerCase() : "";

        for (String keyword : queryKeywords) {
            String k = keyword.toLowerCase();

            // 1. T (Title Score): 關鍵字出現在標題中 [cite: 54]
            // 如果標題包含關鍵字，直接給予高分 (這邊假設出現一次算 1 分，可依需求調整為出現次數)
            double tScore = titleLower.contains(k) ? 1.0 : 0.0;

            // 2. F & O (Content Score): 關鍵字出現在內文中
            double fScore = 0.0;
            double oScore = 0.0;
            
            int freq = contentFreq.getOrDefault(k, 0);
            
            if (freq > 0) {
                if (extractor.isFunctionalKeyword(k)) {
                    // F: 功能性關鍵字 (保濕、美白...) [cite: 55]
                    fScore = freq; 
                } else {
                    // O: 其他關鍵字 [cite: 56]
                    oScore = freq;
                }
            }

            // 套用公式：0.7T + 0.25F + 0.05O
            // 這裡針對"每一個關鍵字"累加分數
            double keywordScore = (WEIGHT_TITLE * tScore) + 
                                  (WEIGHT_FUNCTIONAL * fScore) + 
                                  (WEIGHT_OTHER * oScore);
            
            contentScore += keywordScore;
        }

        // --- 第二部分：層級傳遞 (Child Page Score) ---
        // 依據 PDF[cite: 40, 46]: 0.3 * sum(ChildScore)
        // 注意：這裡假設 cosmeticQuery 在外部已經有把子頁面的分數算好暫存在某處
        // 由於我們目前架構是單層 list，這裡先以「子連結數量」或「預設值」做簡單模擬
        // 若日後有遞迴爬取子頁面，可將 childPages 的 score 加總傳進來
        double childScoreTotal = 0.0;
        
        // (進階實作預留)：如果有子頁面物件
        // if (page.children != null) {
        //    for (PageResult child : page.children) {
        //        childScoreTotal += child.score;
        //    }
        // }
        
        // 目前暫時策略：如果該頁面有很多相關子連結，給予微量加分 (模擬 Link Hub 概念)
        if (page.subLinks != null) {
            // 簡單模擬：每個子連結算 0.1 分 (避免過大)，然後乘上 0.3 權重
            childScoreTotal = page.subLinks.size() * 0.1;
        }

        // 最終總分
        return contentScore + (WEIGHT_CHILD_PAGE * childScoreTotal);
    }
}
/*
9. ProductRanker（排序器）
◎ Variables

mainPageWeight = 0.7

subPageWeight = 0.3

keywordWeights（可選）

◎ Methods
rankResults(List<PageResult> pages)

對每頁呼叫 calculateWeight(page)

計算每頁的總分

依分數排序（高 → 低）

回傳前 N 筆（例如 top 10）

calculateWeight(PageResult page)

→ 核心演算法

主頁分數 = page.mainContentScore × 0.7

子頁分數（子連結內容） × 0.3

再加入 keyword match bonus

回傳 final score */