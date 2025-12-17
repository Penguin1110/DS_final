package dsfinal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GoogleApiService {

    // 改為實例變數，不再寫死
    private String apiKey;
    private String cxId;

    private final HttpClient client;

    public GoogleApiService() {
        this.client = HttpClient.newHttpClient();
        // 初始化時載入設定
        loadConfig();
    }

    /**
     * 從 config.properties 讀取 API Key 與 CX ID
     */
    private void loadConfig() {
        Properties prop = new Properties();
        // 使用 ClassLoader 讀取 classpath 下的檔案
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("❌ 無法找到 config.properties，請確保檔案在 src 或 resources 資料夾下");
                return;
            }
            prop.load(input);
            this.apiKey = prop.getProperty("google.api.key");
            this.cxId = prop.getProperty("google.cx.id");
        } catch (IOException ex) {
            System.err.println("❌ 讀取設定檔失敗");
            ex.printStackTrace();
        }
    }

    public List<String> search(String query) {
        List<String> resultUrls = new ArrayList<>();
        
        // 檢查 Key 是否有讀取成功
        if (apiKey == null || cxId == null) {
            System.err.println("⚠️ API Key 或 CX ID 未設定，無法使用 Google Search");
            return null; // 回傳 null 讓 Crawler 切換到備用方案
        }

        if (query == null || query.isEmpty()) return resultUrls;

        try {
            System.out.println("--- [API] 呼叫 Google API 搜尋: " + query + " ---");

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            
            // 使用讀取到的變數 (this.apiKey, this.cxId)
            String url = String.format(
                "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s",
                this.apiKey, this.cxId, encodedQuery
            );

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // 成功：解析 JSON
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                if (jsonObject.has("items")) {
                    JsonArray items = jsonObject.getAsJsonArray("items");
                    for (int i = 0; i < items.size(); i++) {
                        JsonObject item = items.get(i).getAsJsonObject();
                        String link = item.get("link").getAsString();
                        if (!link.endsWith(".pdf") && !link.endsWith(".doc")) {
                            resultUrls.add(link);
                        }
                    }
                }
            } else if (response.statusCode() == 429) {
                // 429 = Quota Exceeded (額度用完)
                System.err.println("⚠️ Google API 額度已用完 (HTTP 429)！準備切換備用方案...");
                return null; // 回傳 null 代表需要啟動備用方案
            } else {
                System.err.println("Google API Error: " + response.statusCode());
                return null; // 其他錯誤也切換
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null; // 發生例外也切換
        }
        return resultUrls;
    }
}