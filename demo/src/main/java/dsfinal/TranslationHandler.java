package dsfinal;

import org.jsoup.Jsoup;
import java.net.URLEncoder;
import io.github.cdimascio.dotenv.Dotenv;
public class TranslationHandler {
    // ⚠️ 請在此填入你的 Google Cloud API Key
    private static final String API_KEY = Dotenv.load().get("GOOGLE_API_KEY");

    /**
     * 判斷是否需要翻譯 (若包含英文且無中文，則視為需要翻譯)
     */
    public boolean needsTranslation(String query) {
        if (query == null) return false;
        boolean hasChinese = query.matches(".*[\\u4e00-\\u9fa5].*");
        boolean hasEnglish = query.matches(".*[a-zA-Z].*");
        return !hasChinese && hasEnglish;
    }

    /**
     * 呼叫 Google Translate API (Target = zh-TW)
     */
    public String translateToChinese(String query) {
        if (query == null || query.isEmpty()) return "";

        try {
            // 建構 API URL
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + API_KEY
                       + "&q=" + URLEncoder.encode(query, "UTF-8")
                       + "&target=zh-TW"; // 目標語言：繁體中文

            String jsonResponse = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .execute()
                    .body();

            return parseGoogleJsonResponse(jsonResponse);

        } catch (Exception e) {
            System.err.println("Translation API Failed: " + e.getMessage());
            return query; // 失敗時回傳原文
        }
    }

    private String parseGoogleJsonResponse(String json) {
        // 簡單解析 JSON: {"data": {"translations": [{"translatedText": "..."}]}}
        String marker = "\"translatedText\": \"";
        int start = json.indexOf(marker);
        if (start != -1) {
            start += marker.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                return Jsoup.parse(json.substring(start, end)).text();
            }
        }
        return json;
    }
}