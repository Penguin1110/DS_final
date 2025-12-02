package dsfinal;

public class TranslationHandler {
    
}

/*
3. TranslationHandler（紀）
◎ Variables

Map<String, String> cosmeticDictionary

LanguageDetector detector

Translator externalAPITranslator

◎ Methods
detectLanguage(String text)

→ 使用語言偵測器判斷輸入文字是否為中文或英文
→ 回傳語言標籤（"zh" / "en"）

translate(String query)

→ 中文 → 英文轉換流程

detectLanguage(query)

若是英文 → 直接回傳

呼叫 externalAPITranslator.translate(query)

用 cosmeticDictionary 修正（如 "乳液" → "lotion"）

移除多餘字詞（像 "the", "a", "really"）

回傳 final 英文查詢字串
*/