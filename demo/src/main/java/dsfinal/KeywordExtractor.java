package dsfinal;

public class KeywordExtractor {
    
}

/*
8. KeywordExtractor
◎ Variables

stopWords

Stemmer（可選）

Map<String, Integer> frequencyMap

◎ Methods
extractKeywords(String content)

→ 從內容取出所有詞與出現次數

tokenize

lowercase

過濾停用字

統計詞頻頻率

回傳 Map<keyword, freq>

calculateScore(List<String> queryKeywords, Map<String, Integer> freq)

→ 為每個 keyword 計算與 query 的相關分數

基礎 score = freq * 基本權重

若 keyword 在 query 中 → 額外加分

若 keyword 在 title 出現 → 再加強權重

回傳 final score */