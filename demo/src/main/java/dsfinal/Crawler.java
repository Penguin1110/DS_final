package dsfinal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Crawler {
    private HtmlHandler htmlHandler;

    // ============ 1. 高效能設定 (源自 WebCrawler.java) ============
    private static final int CONNECT_TIMEOUT_SECONDS = 5;
    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    
    // 共享 HttpClient (支援 HTTP/2, 連線池)
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .version(HttpClient.Version.HTTP_2)
        .build();

    // User-Agent 輪替清單 (抗封鎖)
    private static final List<String> USER_AGENTS = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"
    );
    private final Random random = new Random();

    public Crawler(HtmlHandler htmlHandler) {
        this.htmlHandler = htmlHandler;
    }

    /**
     * 批量抓取入口 (保留你原本的邏輯)
     */
    public List<PageResult> fetchBatch(List<String> urls) {
        List<PageResult> results = new ArrayList<>();
        for (String url : urls) {
            // 深度設為 3 (抓主頁 + 三層子連結) - 保持不變
            PageResult page = crawl(url, 3);
            if (page != null) {
                results.add(page);
            }
        }
        return results;
    }

    /**
     * 遞迴抓取核心 (保留你的遞迴邏輯，但底層換成高效能 HttpClient)
     */
    private PageResult crawl(String url, int depth) {
        System.out.println("Crawling (Depth " + depth + "): " + url);
        
        // 使用新版的重試機制來獲取 HTML
        String htmlContent = fetchHtmlWithRetry(url, 2); // 最多重試 2 次
        
        if (htmlContent == null || htmlContent.isEmpty()) {
            return null; // 抓取失敗
        }

        // 解析 HTML (使用原本的 HtmlHandler)
        PageResult page = htmlHandler.parseResults(htmlContent, url);

        // 遞迴抓取子連結 (你的核心邏輯，完全保留)
        if (depth > 0 && page.subLinks != null) {
            int count = 0;
            int limit = 4; // 你的限制設定

            for (String subLink : page.subLinks) {
                if (count >= limit) break;
                
                // 禮貌性延遲 (保留你的設定)
                try { Thread.sleep(1000); } catch (InterruptedException e) {}

                PageResult child = crawl(subLink, depth - 1);
                if (child != null) {
                    page.addChildPage(child);
                    count++;
                }
            }
        }
        return page;
    }

    /**
     * ============ 2. 核心升級：帶重試機制的 HTTP 請求 ============
     * (移植自 WebCrawler.java 的 crawlWithRetry 邏輯)
     */
    private String fetchHtmlWithRetry(String url, int maxRetries) {
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                // 輪替 User-Agent
                String userAgent = USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9")
                    .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7") // 模擬繁體中文瀏覽器
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                    .GET()
                    .build();
                
                HttpResponse<String> response = HTTP_CLIENT.send(request, 
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                
                int status = response.statusCode();
                
                // 處理 HTTP 狀態碼
                if (status == 200) {
                    return response.body();
                } else if (status == 429) { // Too Many Requests
                    System.err.println("Rate limited (429) for " + url + ", retrying...");
                    Thread.sleep(2000 * (attempt + 1)); // 指數退避等待
                } else if (status >= 500) { // Server Error
                    System.err.println("Server error (" + status + ") for " + url + ", retrying...");
                    Thread.sleep(1000 * (attempt + 1));
                } else {
                    System.err.println("Failed to fetch " + url + ": HTTP " + status);
                    return null; // 404, 403 等錯誤直接放棄
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                System.err.println("Error fetching " + url + " (Attempt " + (attempt + 1) + "): " + e.getMessage());
                // 連線超時或其他錯誤，等待後重試
                try { Thread.sleep(1000); } catch (InterruptedException ie) {}
            }
        }
        return null; // 重試多次後仍失敗
    }
}