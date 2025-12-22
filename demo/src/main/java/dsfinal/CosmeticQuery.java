package dsfinal;

import java.util.ArrayList;
import java.util.List;

public class CosmeticQuery {
    private final Crawler crawler;
    private final ProductRanker ranker;
    

    
    public CosmeticQuery(Crawler crawler, ProductRanker ranker) {
        this.crawler = crawler;
        this.ranker = ranker;
    }

    public List<PageResult> search(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return new ArrayList<>();

        System.out.println("--- 收到關鍵字: " + keywords + " ---");

        // 1. 智慧擴充 
        String queryStr = buildSmartQuery(keywords);
        System.out.println("--- [擴充] 準備搜尋: " + queryStr + " ---");

       
        List<PageResult> results = crawler.searchAndCrawl(queryStr);
        
        System.out.println("--- 爬蟲結束，共抓取 " + results.size() + " 筆資料 ---");

        // 3. 排名
        return ranker.rankResults(results, keywords);
    }

    private String buildSmartQuery(List<String> keywords) {
        List<String> expandedKeywords = new ArrayList<>(keywords);

        // 【全域脈絡注入】檢查是否有美妝相關詞
        boolean hasContext = keywords.stream().anyMatch(k -> 
            k.contains("妝") || k.contains("粉底") || k.contains("口紅") || 
            k.contains("眼影") || k.contains("腮紅") || k.contains("唇")
        );

        if (!hasContext) {
            expandedKeywords.add("彩妝");
            expandedKeywords.add("化妝品"); 
        }

        
        return String.join(" ", expandedKeywords);
    }
}