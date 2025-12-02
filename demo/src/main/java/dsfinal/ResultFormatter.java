package dsfinal;

public class ResultFormatter {
    
}

/*10. ResultFormatter
◎ Variables

maxItems = 10

JsonBuilder

◎ Methods
format(List<PageResult> results)

→ 整理後端頁面物件 → 前端所需格式

限制筆數

為每個結果生成摘要（前 100 字）

包裝成標準格式（title, summary, url, score）

附上關鍵字推薦（如需）

回傳結果物件

toJSON(Object data)

→ 將結果轉為 JSON
（讓 Controller 回傳給前端） */