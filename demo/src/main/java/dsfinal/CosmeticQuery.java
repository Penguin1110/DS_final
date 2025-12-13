package dsfinal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CosmeticQuery {
    private final Crawler crawler;
    private final ProductRanker ranker;
    private final List<String> baseUrls;

    public CosmeticQuery(Crawler crawler, ProductRanker ranker, List<String> baseUrls) {
        this.crawler = crawler;
        this.ranker = ranker;
        this.baseUrls = baseUrls;
    }

    public List<PageResult> search(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return new ArrayList<>();

        // 1. 產生 URL
        List<String> urls = buildQueryUrls(keywords);

        // 2. 爬蟲 (含遞迴)
        List<PageResult> results = crawler.fetchBatch(urls);

        // 3. 排名
        return ranker.rankResults(results, keywords);
    }

    private List<String> buildQueryUrls(List<String> keywords) {
        List<String> queryUrls = new ArrayList<>();
        // 用空格連接關鍵字
        String queryStr = String.join(" ", keywords);
        try {
            // URL Encode (中文轉 %xx)
            String encoded = URLEncoder.encode(queryStr, StandardCharsets.UTF_8.toString());
            for (String base : baseUrls) {
                queryUrls.add(base + encoded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryUrls;
    }
}