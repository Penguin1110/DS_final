package dsfinal;

import java.util.ArrayList;
import java.util.List;

public class ResultFormatter {

    public SearchResult format(List<PageResult> pageResults) {
        List<SearchResultItem> items = new ArrayList<>();
        int limit = Math.min(pageResults.size(), 10); // 取前 10

        for (int i = 0; i < limit; i++) {
            PageResult page = pageResults.get(i);
            String summary = page.content.length() > 100 
                ? page.content.substring(0, 100) + "..." 
                : page.content;

            items.add(new SearchResultItem(page.title, page.url, summary, page.score));
        }
        return new SearchResult("success", items.size(), items);
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

        public SearchResultItem(String title, String url, String summary, double score) {
            this.title = title;
            this.url = url;
            this.summary = summary;
            this.score = score;
        }
    }
}