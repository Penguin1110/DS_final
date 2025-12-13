package dsfinal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class HtmlHandler {

    public PageResult parseResults(String html, String url) {
        try {
            Document doc = Jsoup.parse(html, url);
            String title = doc.title();
            if (title == null || title.isEmpty()) {
                Element h1 = doc.selectFirst("h1");
                title = (h1 != null) ? h1.text() : "No Title";
            }

            // 這裡呼叫改進過的 extractSubLinks
            List<String> subLinks = extractSubLinks(doc);

            doc.select("script, style, nav, footer, iframe, meta, link").remove();
            String content = doc.body().text();

            return new PageResult(title, content, url, subLinks);

        } catch (Exception e) {
            return new PageResult("Error", "", url, new ArrayList<>());
        }
    }

    private List<String> extractSubLinks(Document doc) {
        List<String> links = new ArrayList<>();
        // 策略：抓取所有連結，但透過嚴格篩選只留商品
        Elements aTags = doc.select("a[href]"); 

        for (Element link : aTags) {
            String absUrl = link.attr("abs:href");
            if (isValidLink(absUrl)) {
                links.add(absUrl);
            }
        }
        return links;
    }

    /**
     * 關鍵修改：過濾導覽列，只抓商品頁
     */
    private boolean isValidLink(String url) {
        if (url == null || url.isEmpty()) return false;
        if (!url.startsWith("http")) return false;
        
        String lower = url.toLowerCase();

        // 1. 基本檔案過濾
        if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".pdf") || lower.endsWith(".css") || lower.endsWith(".js")) return false;

        // 2. 嚴格過濾導覽列與功能連結 (黑名單)
        if (lower.contains("login") || lower.contains("register") || lower.contains("member") || 
            lower.contains("forgot") || lower.contains("contact") || lower.contains("about") || 
            lower.contains("policy") || lower.contains("terms") || lower.contains("faq") ||
            lower.contains("facebook") || lower.contains("instagram") || lower.contains("line") ||
            lower.contains("youtube") || lower.contains("google")) { // 這裡過濾 google 是為了不抓 google 自己的設定頁
            return false;
        }

        // 3. 針對 UrCosme/Cosme 的特定垃圾過濾 (根據你的 Log)
        // 我們不要 "beauty-awards" 也不要 "brands" (品牌總覽)，我們要的是具體的 "products"
        if (lower.contains("beauty-awards") || lower.contains("brands") || lower.contains("ranking")) {
            return false;
        }

        // 4. (進階) 白名單策略：如果網址包含特定特徵才要
        // UrCosme 的商品頁通常包含 "/products/"
        // Google 搜尋出的外部連結通常不包含 "google.com"
        
        // 如果是 Cosme 網站，我們只想要商品頁
        if (lower.contains("cosme.net.tw") || lower.contains("urcosme.com")) {
            // 如果網址不包含 products，就丟掉 (這樣最準！)
            if (!lower.contains("products")) {
                return false;
            }
        }

        return true;
    }
}