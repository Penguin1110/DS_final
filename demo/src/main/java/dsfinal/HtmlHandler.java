package dsfinal;

public class HtmlHandler {
    
}

/*
7. HtmlHandler
◎ Variables

HtmlParser parser（例如 Jsoup）

HTML selector 設定（標題、內文、子連結）

◎ Methods
parseResults(String html)

→ 抽取頁面資訊

用 parser 讀取 HTML

抽取 title（依 selector）

抽取主要內容（文章文字）

移除 script/style

清洗 HTML → 純文字

回傳 PageResult(title, content, url, subLinks)

extractSubLinks(String html)

→ 抽取子連結（例如內頁或討論串）

找所有 <a> tag

過濾掉外站或廣告連結

回傳 List<String> */