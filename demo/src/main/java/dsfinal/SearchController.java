package dsfinal;

import org.springframework.web.bind.annotation.*;
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
      
        this.translationHandler = new TranslationHandler();
        this.encoder = new OneHotEncoder();
        this.formatter = new ResultFormatter();
        
        HtmlHandler htmlHandler = new HtmlHandler();
        KeywordExtractor extractor = new KeywordExtractor();

        Crawler crawler = new Crawler(htmlHandler);
        ProductRanker ranker = new ProductRanker(extractor);

        // ✅ 新版：只要傳入 crawler 和 ranker，因為 CosmeticQuery 會自動產生網址
        this.cosmeticQuery = new CosmeticQuery(crawler, ranker);
    }

    @GetMapping("/search")
    public ResultFormatter.SearchResult search(@RequestParam(value = "q", required = false) String query) {
        System.out.println("收到原始查詢: " + query);
        
        if (query == null || query.trim().isEmpty()) {
            return formatter.formatError("查詢不可為空", 400);
        }

        try {
            // A. 翻譯層
            if (translationHandler.needsTranslation(query)) {
                System.out.println("偵測到外語，翻譯中...");
                query = translationHandler.translateToChinese(query);
                System.out.println("翻譯後查詢: " + query);
            }

            // B. 編碼 
            List<String> keywords = encoder.encode(query);
            System.out.println("搜尋關鍵字: " + keywords);

            // C. 執行搜尋 
            List<PageResult> results = cosmeticQuery.search(keywords);

            if (results.isEmpty()) {
                return formatter.formatError("查無資料", 404);
            }


            //呼叫 formatter.format 時，自動去 PageResult 抓取 top keywords
            // 並放進 JSON 的 relatedKeywords 欄位回傳給前端
            return formatter.format(results);

        } catch (Exception e) {
            e.printStackTrace();
            return formatter.formatError("系統錯誤: " + e.getMessage(), 500);
        }
    }
}