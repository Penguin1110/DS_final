package dsfinal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultFormatter {

    // 修改方法簽章：多接收一個 queryKeywords 參數，用來做智慧摘要分析
    public SearchResult format(List<PageResult> pageResults, List<String> queryKeywords) {
        List<SearchResultItem> items = new ArrayList<>();
        int limit = Math.min(pageResults.size(), 20); 

        for (int i = 0; i < limit; i++) {
            PageResult page = pageResults.get(i);
            
            // 呼叫智慧摘要生成器
            String summary = generateSmartSummary(page.content, queryKeywords, page.keywords);

            // 取出頻率最高的前 3 個關鍵字 (給前端標籤用)
            List<String> topKeywords = getTopKeywords(page.keywords, 3);

            items.add(new SearchResultItem(page.title, page.url, summary, page.score, topKeywords));
        }
        return new SearchResult("success", items.size(), items);
    }

    /**
     * 
     * 找出相關的句子
     */
    private String generateSmartSummary(String content, List<String> queryKeywords, Map<String, Integer> pageKeywords) {
        if (content == null || content.isEmpty()) return "暫無詳細內容";

        // 1. 斷句：依照中文標點符號切割 (。！？ 或 換行)
        // regex: [。！？!?\n] 代表句子的結束
        String[] sentences = content.split("[。！？!?\\n\\r]");
        
        String bestSentence = "";
        double maxScore = -1.0;

        for (String sentence : sentences) {
            String s = sentence.trim();
            if (s.length() < 10) continue; // 太短的句子通常是雜訊 (如 "回首頁")，跳過
            
            double score = 0.0;
            String sLower = s.toLowerCase();

            // 2. 計分邏輯
            
            // A. 命中「使用者搜尋詞」 -> 超高分
            if (queryKeywords != null) {
                for (String q : queryKeywords) {
                    if (sLower.contains(q.toLowerCase())) {
                        score += 100.0; 
                    }
                }
            }

            // B. 命中「該頁面高頻關鍵字」 -> 次高分 
            if (pageKeywords != null) {
                for (String k : pageKeywords.keySet()) {
                    if (sLower.contains(k.toLowerCase())) {
                        score += 10.0;
                    }
                }
            }
            
            // C. 位置加權 (文章開頭的句子通常比較重要，給一點微量加分)
            // 這裡簡單實作：我們假設前面的句子稍微好一點點 (非必要，可依需求調整)
            
            // 3. 擂台賽：誰分數高誰就當摘要
            if (score > maxScore) {
                maxScore = score;
                bestSentence = s;
            }
        }

        // 4. 兜底機制 (Fallback)
        // 如果整篇文章都沒分 (maxScore <= 0)，或者找出的句子太長/太短
        if (maxScore <= 0 || bestSentence.isEmpty()) {
            // 回退到舊邏輯：直接切前 100 字
            return content.length() > 100 ? content.substring(0, 100) + "..." : content;
        }

        // 5. 格式化輸出 (限制長度，避免摘要把卡片撐爆)
        if (bestSentence.length() > 100) {
            bestSentence = bestSentence.substring(0, 100);
           
        }
        System.out.println(bestSentence);
        return bestSentence + "...";
    }

    private List<String> getTopKeywords(Map<String, Integer> keywordMap, int limit) {
        if (keywordMap == null || keywordMap.isEmpty()) return new ArrayList<>();
        return keywordMap.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public SearchResult formatError(String message, int code) {
        SearchResult r = new SearchResult("error", 0, new ArrayList<>());
        r.errorMessage = message;
        return r;
    }

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