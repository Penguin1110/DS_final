package dsfinal;

public class SearchController {
    
}
/* 
    SearchController（流程總管）
◎ Variables

TranslationHandler translationHandler

OneHotEncoder encoder

CosmeticQuery cosmeticQuery

ResultFormatter formatter

Logger logger

◎ Methods
search(String query)

→ 整個 Search Pipeline 的 orchestrator
步驟如下：

檢查 query 是否為空

呼叫 translationHandler.detectLanguage()

若為中文 → translate()

呼叫 encoder.encode() 取得關鍵詞列表

呼叫 cosmeticQuery.search(keywords) 執行搜尋

若結果為空 → 回傳「查無資料」

呼叫 formatter.format() 將結果轉為輸出格式

回傳 JSON 給前端

handleError(Exception e)

→ 接住所有錯誤
→ 記錄 log
→ 回傳標準錯誤 JSON（message、statusCode）
*/