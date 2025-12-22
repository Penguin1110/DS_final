package dsfinal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultFormatter {

    public SearchResult format(List<PageResult> pageResults) {
        List<SearchResultItem> items = new ArrayList<>();
        // 限制回傳筆數，例如前 20 筆
        int limit = Math.min(pageResults.size(), 20); 

        for (int i = 0; i < limit; i++) {
            PageResult page = pageResults.get(i);
            
            // 處理摘要 (避免過長或空值)
            String summary = (page.content != null && page.content.length() > 100)
                ? page.content.substring(0, 100) + "..." 
                : (page.content != null ? page.content : "");

            List<String> topKeywords = getTopKeywords(page.keywords, 3);

            // 建立包含 keywords 的物件
            items.add(new SearchResultItem(page.title, page.url, summary, page.score, topKeywords));
        }
        return new SearchResult("success", items.size(), items);
    }

 
     
    private List<String> getTopKeywords(Map<String, Integer> keywordMap, int limit) {
        if (keywordMap == null || keywordMap.isEmpty()) return new ArrayList<>();

        return keywordMap.entrySet().stream()
            // 依照頻率降序排列
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) 
            // 取前 N 個
            .limit(limit) 
            // 只取關鍵字名稱
            .map(Map.Entry::getKey) 
            .collect(Collectors.toList());
    }

    public SearchResult formatError(String message, int code) {
        SearchResult r = new SearchResult("error", 0, new ArrayList<>());
        r.errorMessage = message;
        return r;
    }

    // --- 內部類別 ---

    public static class SearchResult {
        public String status;
        public int count;
        public List<SearchResultItem> results;
        public String errorMessage;

        public SearchResult(String status, int count, List<SearchResultItem> results) {
            this.status = status;
            this.count = count;
            this.results = results;
        }
    }

    public static class SearchResultItem {
        public String title;
        public String url;
        public String summary;
        public double score;
        

        public List<String> relatedKeywords; 

        public SearchResultItem(String title, String url, String summary, double score, List<String> relatedKeywords) {
            this.title = title;
            this.url = url;
            this.summary = summary;
            this.score = score;
            this.relatedKeywords = relatedKeywords;
        }
    }
}