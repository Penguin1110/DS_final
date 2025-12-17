package dsfinal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProductRanker {
    private static final double WEIGHT_TITLE = 1.5;       // 提高標題權重
    private static final double WEIGHT_FUNCTIONAL = 0.8;  // 提高功能詞權重
    private static final double WEIGHT_OTHER = 0.2;
    private static final double WEIGHT_CHILD_PAGE = 0.5;  // 提高子頁面回饋權重

    private KeywordExtractor extractor;

    public ProductRanker(KeywordExtractor extractor) {
        this.extractor = extractor;
    }

    public List<PageResult> rankResults(List<PageResult> pages, List<String> queryKeywords) {
        System.out.println("--- 開始評分 (總頁數: " + pages.size() + ") ---");
        
        for (PageResult page : pages) {
            page.score = calculatePageScore(page, queryKeywords);
            
            // Debug: 印出有分數的頁面
            if (page.score >= 0) {
                System.out.printf("   [Score: %.2f] %s (%s)\n", page.score, page.title, page.url);
            }
        }
        
        // 分數高 -> 低
        Collections.sort(pages, (p1, p2) -> Double.compare(p2.score, p1.score));
        return pages;
    }

    private double calculatePageScore(PageResult page, List<String> queryKeywords) {
        if (page == null || queryKeywords == null || queryKeywords.isEmpty()) return 0.0;

        // 1. 計算本頁內容分數
        double contentScore = calculateContentScore(page.title, page.content, queryKeywords);

        // 2. 計算子頁面分數 (遞迴加總)
        double childScoreTotal = 0.0;
        if (page.childPages != null) {
            for (PageResult child : page.childPages) {
                childScoreTotal += calculateContentScore(child.title, child.content, queryKeywords);
            }
        }

        return contentScore + (WEIGHT_CHILD_PAGE * childScoreTotal);
    }

    private double calculateContentScore(String title, String content, List<String> keywords) {
        double score = 0.0;
        Map<String, Integer> freqMap = extractor.extractKeywords(content);
        
        // 轉小寫比對
        String titleLower = (title != null) ? title.toLowerCase() : "";

        for (String k : keywords) {
            String kLower = k.toLowerCase();

            // 標題命中 (只要包含關鍵字就算分)
            double tScore = titleLower.contains(kLower) ? 1.0 : 0.0;

            // 內文命中
            double fScore = 0.0;
            double oScore = 0.0;
            
            // 嘗試取得詞頻 (支援大小寫)
            int freq = freqMap.getOrDefault(k, 0); 
            if (freq == 0) freq = freqMap.getOrDefault(kLower, 0);

            if (freq > 0) {
                // 如果是我們定義的功能詞，分數加倍
                if (extractor.isFunctionalKeyword(k)) {
                    fScore = freq; 
                } else {
                    // 即使不是功能詞，只要是使用者搜的字，也該給基本分 (這是之前的 Bug，導致非功能詞 0 分)
                    oScore = freq; 
                }
            }

            score += (WEIGHT_TITLE * tScore) + 
                     (WEIGHT_FUNCTIONAL * fScore) + 
                     (WEIGHT_OTHER * oScore);
        }
        return score;
    }
}