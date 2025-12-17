package dsfinal;

import java.util.ArrayList;
import java.util.List;

public class CosmeticQuery {
    private final Crawler crawler;
    private final ProductRanker ranker;
    // ❌ 移除 baseUrls，我們不再需要手動組裝搜尋引擎網址了

    // 建構子也變乾淨了，不需要傳入 baseUrls
    public CosmeticQuery(Crawler crawler, ProductRanker ranker) {
        this.crawler = crawler;
        this.ranker = ranker;
    }

    public List<PageResult> search(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return new ArrayList<>();

        System.out.println("--- 收到關鍵字: " + keywords + " ---");

        // 1. 智慧擴充 (只負責組裝成一個漂亮的查詢字串，例如 "橘子 彩妝")
        String queryStr = buildSmartQuery(keywords);
        System.out.println("--- [智慧擴充] 準備搜尋: " + queryStr + " ---");

        // 2. ✅✅✅ 關鍵修改：呼叫 Crawler 的 API 搜尋入口
        // 舊的寫法是 crawler.fetchBatch(urls)，那是錯的！
        // 這裡會觸發 Crawler -> GoogleApiService -> 拿到種子網址 -> 批量爬取
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

        // 直接回傳組裝好的字串 (例如 "橘子 彩妝 化妝品")
        // 不再回傳 List<String> urls，因為我們不需要自己拼 google.com 網址
        return String.join(" ", expandedKeywords);
    }
}