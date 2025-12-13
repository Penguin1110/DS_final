package dsfinal;

import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SearchController {

    private TranslationHandler translationHandler;
    private OneHotEncoder encoder;
    private CosmeticQuery cosmeticQuery;
    private ResultFormatter formatter;

    public SearchController() {
        // 1. 初始化 Helper
        this.translationHandler = new TranslationHandler();
        this.encoder = new OneHotEncoder();
        this.formatter = new ResultFormatter();
        
        HtmlHandler htmlHandler = new HtmlHandler();
        KeywordExtractor extractor = new KeywordExtractor();

        // 2. 組裝 Crawler 與 Ranker
        Crawler crawler = new Crawler(htmlHandler);
        ProductRanker ranker = new ProductRanker(extractor);

        // 3. 設定搜尋來源 (這裡請換成你真正想爬的網站，或者靜態測試頁)
        List<String> baseUrls = Arrays.asList(
            "https://www.google.com/search?q="// 範例
 
        );

        // 4. 初始化主流程
        this.cosmeticQuery = new CosmeticQuery(crawler, ranker, baseUrls);
    }

    @GetMapping("/search")
    public ResultFormatter.SearchResult search(@RequestParam(value = "q", required = false) String query) {
        System.out.println("收到原始查詢: " + query);
        
        if (query == null || query.trim().isEmpty()) {
            return formatter.formatError("查詢不可為空", 400);
        }

        try {
            // A. 翻譯層 (英文 -> 中文)
            if (translationHandler.needsTranslation(query)) {
                System.out.println("偵測到英文，翻譯中...");
                query = translationHandler.translateToChinese(query);
                System.out.println("翻譯後查詢: " + query);
            }

            // B. 編碼 (中文 -> 關鍵字列表)
            List<String> keywords = encoder.encode(query);
            System.out.println("搜尋關鍵字: " + keywords);

            // C. 執行搜尋 (含遞迴爬取 + 排名)
            List<PageResult> results = cosmeticQuery.search(keywords);

            if (results.isEmpty()) {
                return formatter.formatError("查無資料", 404);
            }

            // D. 回傳結果
            return formatter.format(results);

        } catch (Exception e) {
            e.printStackTrace();
            return formatter.formatError("系統錯誤: " + e.getMessage(), 500);
        }
    }
}