package dsfinal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlHandler {

    public PageResult parseResults(String html, String url) {
        try {
            Document doc = Jsoup.parse(html, url);

            String title = doc.title();
            if (title == null || title.isEmpty()) {
                Element h1 = doc.selectFirst("h1");
                title = (h1 != null) ? h1.text() : "No Title";
            }

     

            if (isJunkPage(title)) {
                System.out.println("   -> [Junk] 標題過濾 (登入/無關頁面): " + title);
                // 回傳空物件，Crawler 收到後會忽略它
                return new PageResult(title, "", url, new ArrayList<>());
            }

            List<String> subLinks = extractSubLinks(doc);

            // 清洗雜訊
            doc.select("script, style, nav, footer, iframe, meta, link, header").remove(); // 多移除 header
            String content = doc.body().text();

            //內容太短
            if (content.length() < 10) {
                 return new PageResult(title, "", url, new ArrayList<>());
            }

            return new PageResult(title, content, url, subLinks);

        } catch (Exception e) {
            return new PageResult("Error", "", url, new ArrayList<>());
        }
    }

    private boolean isJunkPage(String title) {
        String t = title.toLowerCase();
        return t.contains("登入") || t.contains("註冊") || t.contains("會員") || 
               t.contains("login") || t.contains("sign in") || t.contains("sign up") ||
               t.contains("password") || t.contains("購物車") || t.contains("結帳") ||
               t.contains("cart") || t.contains("checkout") || 
               t.contains("404") || t.contains("not found") || 
               t.contains("google 搜尋") || t.contains("verify") || t.contains("access denied");
    }

    private List<String> extractSubLinks(Document doc) {
        List<String> links = new ArrayList<>();
        Elements aTags = doc.select("a[href]");

        for (Element link : aTags) {
            String href = link.attr("abs:href");

            // DuckDuckGo 解碼
            if (href.contains("uddg=")) {
                String decoded = extractDDGTarget(href);
                if (decoded != null) href = decoded;
            }


            if (isValidLink(href)) {
                if (!links.contains(href)) {
                    links.add(href);
                }
            }
        }
        return links;
    }

    private String extractDDGTarget(String ddgUrl) {
        try {
            Pattern pattern = Pattern.compile("uddg=([^&]+)");
            Matcher matcher = pattern.matcher(ddgUrl);
            if (matcher.find()) {
                return URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.toString());
            }
        } catch (Exception e) {}
        return null;
    }

    private boolean isValidLink(String url) {
        if (url == null || url.isEmpty()) return false;
        if (!url.startsWith("http")) return false;

        String lower = url.toLowerCase();

        // 1. 絕對不爬的網域
        if (lower.contains("google.") || lower.contains("duckduckgo.") || lower.contains("bing.") || lower.contains("yahoo.")) return false;
        if (lower.contains("facebook.") || lower.contains("instagram.") || lower.contains("twitter.") || lower.contains("youtube.")) return false;
        if (lower.contains("dcard.tw/f/") && !lower.contains("/p/")) return false; // Dcard 只爬文章(/p/)，不爬列表(/f/)

        // 2. 絕對不爬的功能性頁面
        if (lower.contains("login") || lower.contains("signin") || lower.contains("sign-in") || 
            lower.contains("signup") || lower.contains("sign-up") || lower.contains("register") || 
            lower.contains("member") || lower.contains("account") || lower.contains("my-account") ||
            lower.contains("cart") || lower.contains("checkout") || lower.contains("basket") || 
            lower.contains("password") || lower.contains("reset") || lower.contains("auth")) {
            return false;
        }

        // 3. 檔案過濾
        if (lower.endsWith(".pdf") || lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".zip")) return false;

        return true;
    }
}