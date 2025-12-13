package dsfinal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProductRanker {
    // 依據你的需求設定權重
    private static final double WEIGHT_TITLE = 0.7;       // T: 主標題
    private static final double WEIGHT_FUNCTIONAL = 0.25; // F: 功能性 (保濕、控油...)
    private static final double WEIGHT_OTHER = 0.05;      // O: 其他 (品牌、刷具...)
    private static final double WEIGHT_CHILD_PAGE = 0.3;  // 子頁面

    private KeywordExtractor extractor;

    public ProductRanker(KeywordExtractor extractor) {
        this.extractor = extractor;
    }

    public List<PageResult> rankResults(List<PageResult> pages, List<String> queryKeywords) {
        for (PageResult page : pages) {
            page.score = calculatePageScore(page, queryKeywords);
        }
        Collections.sort(pages, (p1, p2) -> Double.compare(p2.score, p1.score));
        return pages;
    }

    private double calculatePageScore(PageResult page, List<String> queryKeywords) {
        if (page == null || queryKeywords == null || queryKeywords.isEmpty()) return 0.0;

        double contentScore = calculateContentScore(page.title, page.content, queryKeywords);

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
        // 取得內文的詞頻表
        Map<String, Integer> freqMap = extractor.extractKeywords(content);
        String titleLower = (title != null) ? title.toLowerCase() : "";

        for (String k : keywords) {
            String kLower = k.toLowerCase();

            // 1. T (Title Score): 標題命中 (0.7)
            double tScore = titleLower.contains(kLower) ? 1.0 : 0.0;

            // 2. F & O (Content Score): 內文命中
            double fScore = 0.0;
            double oScore = 0.0;

            // 檢查該關鍵字在內文出現的次數
            // 注意：我們搜尋的是使用者輸入的關鍵字(Query)，
            // 但如果使用者的關鍵字剛好也是我們的「功能詞」或「其他詞」，就給予對應加權。
            
            int freq = freqMap.getOrDefault(k, 0); // 嘗試拿原始大小寫
            if (freq == 0) freq = freqMap.getOrDefault(kLower, 0); // 拿不到試試小寫

            if (freq > 0) {
                if (extractor.isFunctionalKeyword(k)) {
                    // F: 功能性關鍵字 (0.25)
                    fScore = freq; 
                } else if (extractor.isOtherKeyword(k)) {
                    // O: 其他關鍵字/品牌 (0.05)
                    oScore = freq;
                } else {
                    // 如果是分類關鍵字 (Category) 出現在「內文」中，
                    // 通常視為基本關聯，這裡我們可以預設給它跟 F 一樣甚至更高的分數，
                    // 或者依據公式，如果題目沒定義 Category 在內文的權重，
                    // 我們可以把它歸類在 F (視為 User Intent 的一部分) 或 O。
                    // 這裡為了保守起見，若 Query 是產品分類 (如"粉底液") 出現在內文中，我們算作 0.1 (介於 F 和 O 之間)
                    // 或是直接將其視為 O (0.05) 避免分數過度膨脹。
                    oScore = freq; 
                }
            }

            // 套用公式： 0.7*T + 0.25*F + 0.05*O
            score += (WEIGHT_TITLE * tScore) + 
                     (WEIGHT_FUNCTIONAL * fScore) + 
                     (WEIGHT_OTHER * oScore);
        }
        return score;
    }
}