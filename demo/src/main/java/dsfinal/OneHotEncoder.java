package dsfinal;

public class OneHotEncoder {
    
}

/*
4. OneHotEncoder（紀）
◎ Variables

List<String> stopWords

Tokenizer tokenizer

◎ Methods
encode(String text)

→ 將英文 query 轉成關鍵字列表

tokenize → 分詞（依空白與符號）

lowercase → 全部變小寫

去掉標點符號

過濾 stopwords（the, with, of…）

移除空字串

回傳 List<String> keywords */