package dsfinal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Crawler {
    private HtmlHandler htmlHandler;
    private GoogleApiService googleService;

    // 🚀 優化 1: 建立執行緒池 (同時允許 10 個請求並發)
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3)) // 🚀 優化 2: 連線逾時縮短為 3 秒 (太慢就跳過)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .version(HttpClient.Version.HTTP_2)
        .build();

    private static final List<String> USER_AGENTS = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    );
    private final Random random = new Random();

    public Crawler(HtmlHandler htmlHandler) {
        this.htmlHandler = htmlHandler;
        this.googleService = new GoogleApiService();
    }

    public List<PageResult> searchAndCrawl(String query) {
        List<String> seedUrls = null;

        try {
            System.out.println("--- 嘗試使用 Google API 搜尋 ---");
            seedUrls = googleService.search(query);
        } catch (Exception e) {
            System.err.println("Google API 連線異常");
        }

        if (seedUrls == null || seedUrls.isEmpty()) {
            System.out.println("--- ⚠️ 啟動備用方案: 爬取 DuckDuckGo HTML ---");
            seedUrls = scrapeFallbackUrls(query);
        }

        if (seedUrls.isEmpty()) return new ArrayList<>();

        // 🚀 只取前 6 個結果來爬，避免太多垃圾資訊拖慢速度
        if (seedUrls.size() > 6) {
            seedUrls = seedUrls.subList(0, 6);
        }

        return fetchBatch(seedUrls);
    }

    // ✅ 核心修改：平行爬取 (Parallel Crawling)
    public List<PageResult> fetchBatch(List<String> urls) {
        // 使用 CompletableFuture 讓所有網址同時開爬
        List<CompletableFuture<PageResult>> futures = urls.stream()
            .map(url -> CompletableFuture.supplyAsync(() -> {
                // 🚀 優化 3: 深度改為 1 (只抓 Google 給的頁面，不再往下抓子連結)
                // 如果你還是想抓子頁面，改成 2，但速度會慢 3 倍
                return crawl(url, 2); 
            }, executor))
            .collect(Collectors.toList());

        // 等待所有爬蟲回來，並攤平結果
        return futures.stream()
            .map(CompletableFuture::join) // 等待結果
            .filter(Objects::nonNull)
            .map(root -> {
                List<PageResult> flat = new ArrayList<>();
                collectAllPages(root, flat);
                return flat;
            })
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    private void collectAllPages(PageResult node, List<PageResult> list) {
        list.add(node);
        if (node.childPages != null) {
            for (PageResult child : node.childPages) {
                collectAllPages(child, list);
            }
        }
    }

    private PageResult crawl(String url, int depth) {
        System.out.println("🚀 Crawling: " + url);
        String html = fetchHtml(url);
        if (html == null) return null;

        PageResult page = htmlHandler.parseResults(html, url);
        
        // 🚀 優化 4: 只有當 depth > 1 時才去抓子連結，且限制數量為 2
        if (depth > 1 && page.subLinks != null) {
            int count = 0;
            for (String subLink : page.subLinks) {
                if (count >= 5) break; // 每頁最多只抓 2 個子連結 (夠了)
                
                // 子連結就不開執行緒了，避免爆炸
                PageResult child = crawl(subLink, depth - 1);
                if (child != null) {
                    page.addChildPage(child);
                    count++;
                }
            }
        }
        return page;
    }

    private String fetchHtml(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENTS.get(0))
                .timeout(Duration.ofSeconds(4)) // 單頁請求最多等 4 秒
                .GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) return response.body();
        } catch (Exception e) { 
            // 忽略錯誤，繼續下一個
        }
        return null;
    }

    // ... scrapeFallbackUrls 和 extractDDGTarget 保持不變 ...
    private List<String> scrapeFallbackUrls(String query) {
        List<String> urls = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUrl = "https://html.duckduckgo.com/html/?q=" + encodedQuery;
            Document doc = Jsoup.connect(searchUrl).userAgent(USER_AGENTS.get(0)).timeout(5000).get();
            Elements links = doc.select("a.result__a");
            for (Element link : links) {
                String href = link.attr("abs:href");
                if (href.contains("uddg=")) href = extractDDGTarget(href);
                if (href != null && href.startsWith("http")) urls.add(href);
            }
        } catch (Exception e) {}
        return urls;
    }

    private String extractDDGTarget(String ddgUrl) {
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("uddg=([^&]+)");
            java.util.regex.Matcher m = p.matcher(ddgUrl);
            if (m.find()) return URLDecoder.decode(m.group(1), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {}
        return null;
    }
}