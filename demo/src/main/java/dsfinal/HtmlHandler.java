package dsfinal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlHandler {
    // 可以在這裡設定針對特定網站的 Selector，目前先用通用的
    private String titleSelector = "title"; 

    /**
     * 解析 HTML 字串，轉成 PageResult 物件
     * @param html 爬蟲抓下來的 HTML 原始碼
     * @param url 該頁面的網址 (用來處理相對路徑連結)
     */
    public PageResult parseResults(String html, String url) {
        try {
            // 1. 使用 Jsoup 解析 HTML
            Document doc = Jsoup.parse(html, url);

            // 2. 抽取標題
            String title = doc.title();
            if (title == null || title.isEmpty()) {
                // 如果沒有 title tag，試著找 h1
                Element h1 = doc.selectFirst("h1");
                title = (h1 != null) ? h1.text() : "No Title";
            }

            // 3. 抽取子連結 (呼叫下方的輔助方法)
            List<String> subLinks = extractSubLinks(doc);

            // 4. 清洗內容 (這步最關鍵，去雜訊)
            // 移除 script, style, nav, footer 等非主要內容標籤
            doc.select("script, style, nav, footer, iframe, meta, link").remove();
            
            // 取得純文字內容 (text() 會自動去掉 HTML tag)
            String content = doc.body().text();

            return new PageResult(title, content, url, subLinks);

        } catch (Exception e) {
            e.printStackTrace();
            // 解析失敗回傳空物件，避免當機
            return new PageResult("Error", "", url, new ArrayList<>());
        }
    }

    /**
     * 抽取頁面中所有的有效連結 (<a> tag)
     */
    private List<String> extractSubLinks(Document doc) {
        List<String> links = new ArrayList<>();
        Elements aTags = doc.select("a[href]"); // 只抓有 href 屬性的 a 標籤

        for (Element link : aTags) {
            // Jsoup 的 abs:href 會自動幫你把相對路徑轉成絕對路徑 (http://...)
            String absUrl = link.attr("abs:href");

            // 簡單過濾：只留 http/https，且過濾掉過短或顯然是廣告的連結
            if (isValidLink(absUrl)) {
                links.add(absUrl);
            }
        }
        return links;
    }

    /**
     * 連結過濾規則 (可依需求擴充)
     */
    private boolean isValidLink(String url) {
        if (url == null || url.isEmpty()) return false;
        if (!url.startsWith("http")) return false;
        
        // 過濾掉常見的圖片、檔案、或社群分享連結
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".pdf")) return false;
        if (lowerUrl.contains("facebook.com/sharer") || lowerUrl.contains("twitter.com/share")) return false;

        return true;
    }
}