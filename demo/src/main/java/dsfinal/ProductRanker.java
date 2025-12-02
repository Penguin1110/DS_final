package dsfinal;

public class ProductRanker {
    
}
/*
9. ProductRanker（排序器）
◎ Variables

mainPageWeight = 0.7

subPageWeight = 0.3

keywordWeights（可選）

◎ Methods
rankResults(List<PageResult> pages)

對每頁呼叫 calculateWeight(page)

計算每頁的總分

依分數排序（高 → 低）

回傳前 N 筆（例如 top 10）

calculateWeight(PageResult page)

→ 核心演算法

主頁分數 = page.mainContentScore × 0.7

子頁分數（子連結內容） × 0.3

再加入 keyword match bonus

回傳 final score */