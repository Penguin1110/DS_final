package dsfinal;

public class Crawler {
    
}

/*
6. Crawler（爬蟲）
◎ Variables

HttpClient client

int timeout

String userAgent

◎ Methods
fetch(String url)

→ 抓單頁 HTML

建立 HTTP GET request

設定 timeout、header（user-agent）

傳送 request

回傳 HTML string（或錯誤）

fetchBatch(List<String> urls)

→ 批次抓取，多執行緒（速度關鍵）

對每個 URL 使用 executor 建多個 task

並行抓取 HTML（提升速度）

收集所有成功的 HTML

回傳 List<String> */