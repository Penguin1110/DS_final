package dsfinal;

public class CosmeticQuery {
    //123
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