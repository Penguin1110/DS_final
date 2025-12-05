package dsfinal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Crawler {
    private final int timeout = 6000;
    private final String userAgent = "Mozilla/5.0";

    /**
     * 單頁抓取
     * 負責處理 HTTP 連線、轉址 (Redirect) 與讀取內容
     */
    public String fetch(String urlStr) {
        String currentUrl = urlStr;
        int hops = 0;
        
        // 允許最多 5 次轉址，避免無窮迴圈
        while (hops < 5) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(currentUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false); // 手動處理轉址
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("User-Agent", userAgent);

                int code = conn.getResponseCode();

                // 處理 3xx 轉址
                if (code >= 300 && code < 400) {
                    String newLocation = conn.getHeaderField("Location");
                    if (newLocation == null || newLocation.isEmpty()) {
                        return "";
                    }
                    // 處理相對路徑轉絕對路徑
                    URL absoluteUrl = new URL(new URL(currentUrl), newLocation);
                    currentUrl = absoluteUrl.toString();
                    hops++;
                    conn.disconnect();
                    continue;
                }

                // 處理 4xx / 5xx 錯誤 (視為抓取失敗)
                if (code >= 400) {
                    return "";
                }

                // 讀取成功內容
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString();

            } catch (Exception e) {
                // 發收任何網路錯誤，回傳空字串，確保流程不中斷
                // 實際專案中可以使用 Logger 記錄 e.getMessage()
                return "";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return "";
    }

    /**
     * 批次抓取：使用多執行緒並行處理
     * 依據規格，這能提升速度
     */
    public List<String> fetchBatch(List<String> urls) {
        List<String> results = new ArrayList<>();
        
        // 建立執行緒池 (Thread Pool)，大小設為 URL 數量或固定上限 (例如 10)
        // 這裡設為 10，避免一次發出太多 Request 被擋
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(urls.size(), 10));
        
        List<Future<String>> futures = new ArrayList<>();

        // 1. 為每個 URL 建立一個任務 (Task)
        for (String url : urls) {
            Callable<String> task = () -> fetch(url);
            futures.add(executor.submit(task));
        }

        // 2. 收集結果
        for (Future<String> future : futures) {
            try {
                // get() 會等待該任務完成並取得回傳值
                String html = future.get();
                if (html != null && !html.isEmpty()) {
                    results.add(html);
                }
            } catch (InterruptedException | ExecutionException e) {
                // 若某個執行緒出錯，忽略該筆，繼續處理下一筆
                e.printStackTrace();
            }
        }

        // 3. 關閉執行緒池
        executor.shutdown();
        
        return results;
    }
}