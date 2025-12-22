package dsfinal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProductRanker {
    private static final double WEIGHT_TITLE = 0.75;       // 標題命中權重
    private static final double WEIGHT_FUNCTIONAL = 0.25;  // 搜尋詞是功能詞的權重
    private static final double WEIGHT_OTHER = 0.05;       // 搜尋詞是普通詞的權重
    
    // 額外加分權重 
    private static final double BONUS_FUNCTIONAL = 0.1;   // 文章包含其他功能詞
    
    private static final double WEIGHT_CHILD_PAGE = 0.5;

    private KeywordExtractor extractor;

    public ProductRanker(KeywordExtractor extractor) {
        this.extractor = extractor;
    }

    public List<PageResult> rankResults(List<PageResult> pages, List<String> queryKeywords) {
        System.out.println("--- 開始評分 (總頁數: " + pages.size() + ") ---");
        
        for (PageResult page : pages) {
            // 1. 提取並回存
            Map<String, Integer> mainFreqMap = extractor.extractKeywords(page.content);
            page.keywords = mainFreqMap;

            // 2. 計算分數
            double currentScore = calculateScoreFromFreq(page.title, mainFreqMap, queryKeywords);

            // 3. 子頁面處理
            double childScoreTotal = 0.0;
            if (page.childPages != null) {
                for (PageResult child : page.childPages) {
                    Map<String, Integer> childFreqMap = extractor.extractKeywords(child.content);
                    child.keywords = childFreqMap;
                    double cScore = calculateScoreFromFreq(child.title, childFreqMap, queryKeywords);
                    child.score = cScore;
                    childScoreTotal += cScore;
                }
            }

            page.score = currentScore + (childScoreTotal * WEIGHT_CHILD_PAGE);

            if (page.score > 0) {
                System.out.printf("   [Score: %.2f] %s\n", page.score, page.title);
            }
        }
        
        Collections.sort(pages, (p1, p2) -> Double.compare(p2.score, p1.score));
        return pages;
    }

    private double calculateScoreFromFreq(String title, Map<String, Integer> freqMap, List<String> queryKeywords) {
        if (freqMap == null || freqMap.isEmpty()) return 0.0;
        
        double score = 0.0;
        String titleLower = (title != null) ? title.toLowerCase() : "";


        for (String k : queryKeywords) {
            String kLower = k.toLowerCase();
            
            // 標題命中
            if (titleLower.contains(kLower)) {
                score += (WEIGHT_TITLE * 10.0); // 標題權重放大，確保關聯性
            }

            // 內文命中
            int freq = freqMap.getOrDefault(k, 0);
            if (freq == 0) freq = freqMap.getOrDefault(kLower, 0);

            if (freq > 0) {
                if (extractor.isFunctionalKeyword(k)) {
                    score += freq * WEIGHT_FUNCTIONAL;
                } else {
                    score += freq * WEIGHT_OTHER;
                }
            }
        }


        // 遍歷這篇文章
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();

  
            // 獎勵內容豐富的文章
            boolean isUserQuery = false;
            for (String q : queryKeywords) {
                if (q.equalsIgnoreCase(word)) {
                    isUserQuery = true;
                    break;
                }
            }

            if (!isUserQuery && extractor.isFunctionalKeyword(word)) {

                score += count * BONUS_FUNCTIONAL;
            }
        }

        return score;
    }
}