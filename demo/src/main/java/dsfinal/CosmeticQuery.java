package dsfinal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CosmeticQuery {
    private final Crawler crawler;
    private final HtmlHandler htmlHandler;
    private final KeywordExtractor extractor;
    private final ProductRanker ranker;
    private final List<String> baseUrls;

    public CosmeticQuery(
            Crawler crawler,
            HtmlHandler htmlHandler,
            KeywordExtractor extractor,
            ProductRanker ranker,
            List<String> baseUrls) {
        this.crawler = crawler;
        this.htmlHandler = htmlHandler;
        this.extractor = extractor;
        this.ranker = ranker;
        this.baseUrls = baseUrls;
    }
   /**
     * ◎ Methods: search(List<String> keywords)
     * 整個搜尋主流程（後端核心）：
     * 1. 產生 URL
     * 2. 爬取 HTML
     * 3. 解析並提取關鍵字
     * 4. 排名與排序
     * * @param keywords 使用者的查詢關鍵字列表 (已由 OneHotEncoder 處理)
     * @return 排序後前 N 個 PageResult
     */
    public List<PageResult> search(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return new ArrayList<>();
        }

        System.out.println("--- 1. 建構查詢網址 ---");
        // 呼叫 buildQueryUrls(keywords)
        List<String> urls = buildQueryUrls(keywords);

        System.out.println("--- 2. 即時批量爬取 HTML ---");
        // 用 crawler.fetchBatch(urls) 撈 HTML
        List<String> htmlResults = crawler.fetchBatch(urls);

        if (htmlResults.isEmpty()) {
            System.out.println("查無資料，爬取結果為空。");
            return new ArrayList<>();
        }
        
        // 儲存所有處理完成的 PageResult
        List<PageResult> allPages = new ArrayList<>();
        
        System.out.println("--- 3. 解析、清洗與詞彙分析 ---");
        // 由於 crawler.fetchBatch 不會回傳 URL/HTML 的對應關係，
        // 這裡我們需要一個假設：我們只處理成功抓取到的 HTML 內容，並將 URL 設為空字串或預設值
        // **注意：現實中需要將 URL 和 HTML 綁定。為了符合您的架構，我們假設在解析時，URL 已經被隱式處理或在 HtmlHandler 內被忽略。**

        int urlIndex = 0; // 模擬當前處理的 URL 索引
        for (String html : htmlResults) {
            // 將每個 HTML 丟給 htmlHandler.parseResults() → 獲得頁面初步資料
            // 這裡傳入的 URL 應為 htmlResults[i] 對應的 URL，但因 fetchBatch 限制，暫用一個模擬 URL
            String currentUrl = (urlIndex < urls.size()) ? urls.get(urlIndex++) : "unknown_url";
            
            PageResult page = htmlHandler.parseResults(html, currentUrl);
            
            if (page.content.isEmpty()) continue;

            // 呼叫 extractor.extractKeywords(pageContent) → 計算詞頻/關鍵詞
            // 這一步驟實際上應將詞頻 Map 儲存到 PageResult 供 Ranker 使用，但 Ranker 內部會再呼叫一次 extractor
            // 這裡我們專注在流程控制，不重複儲存。
            
            allPages.add(page);
        }

        System.out.println("--- 4. 專業排名與排序 ---");
        // 呼叫 ranker.rankResults(pages) → 計算得分、排序
        // ProductRanker 需要原始的 PageResult 列表和查詢關鍵字來計算分數。
        List<PageResult> sortedResults = ranker.rankResults(allPages, keywords);

        // 回傳排序後前 N 個結果 (這裡簡單回傳全部，可在外部做截斷)
        return sortedResults;
    }

    /**
     * ◎ Methods: buildQueryUrls(List<String> keywords)
     * 產生所有「搜尋用」網址：對每個 baseUrl 幫每個 keyword 做 URL encode 並組出完整查詢網址。
     * * @param keywords 查詢關鍵字列表
     * @return 完整的查詢網址列表
     */
    private List<String> buildQueryUrls(List<String> keywords) {
        List<String> queryUrls = new ArrayList<>();
        
        // 將所有關鍵字用空格串接起來 (e.g., "保濕 遮瑕")
        String queryStr = String.join(" ", keywords);
        
        try {
            // 對查詢字串進行 URL 編碼 (例如：空格變成 + 或 %20)
            String encodedQuery = URLEncoder.encode(queryStr, StandardCharsets.UTF_8.toString());
            
            // 對每個 base URL 進行組合
            for (String baseUrl : baseUrls) {
                // 假設 baseUrl 類似 "http://example.com/search?q="
                queryUrls.add(baseUrl + encodedQuery);
            }
        } catch (Exception e) {
            System.err.println("URL Encoding Error: " + e.getMessage());
        }

        return queryUrls;
    } 
}
/* 
5. CosmeticQuery（搜尋主邏輯）
◎ Variables

Crawler crawler

HtmlHandler htmlHandler

KeywordExtractor extractor

ProductRanker ranker

List<String> baseUrls

◎ Methods
search(List<String> keywords)

→ 整個搜尋主流程（後端核心）

呼叫 buildQueryUrls(keywords)

用 crawler.fetchBatch(urls) 撈 HTML

將每個 HTML 丟給 htmlHandler.parseResults() → 獲得頁面初步資料

呼叫 extractor.extractKeywords(pageContent) → 計算詞頻/關鍵詞

呼叫 ranker.rankResults(pages) → 計算得分、排序

回傳排序後前 N 個結果

buildQueryUrls(List<String> keywords)

→ 產生所有「搜尋用」網址

對每個 baseUrl（例如 Dcard、PTT、美妝網站）

幫每個 keyword 做 URL encode

組出完整查詢網址

回傳網址列表
*/
