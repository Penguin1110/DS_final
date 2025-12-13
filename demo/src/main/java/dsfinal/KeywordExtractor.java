package dsfinal;

import java.util.*;

public class KeywordExtractor {
    private Set<String> functionalKeywords; // F: 功能性 (權重 0.25)
    private Set<String> categoryKeywords;   // C: 分類詞 (視同高關聯，通常用於標題比對)
    private Set<String> otherKeywords;      // O: 其他/品牌/輔助 (權重 0.05)

    public KeywordExtractor() {
        initializeCategoryKeywords();
        initializeFunctionalKeywords();
        initializeOtherKeywords();
    }

    // --- 1. 分類關鍵字 (對應產品種類) ---
    private void initializeCategoryKeywords() {
        categoryKeywords = new HashSet<>();
        Collections.addAll(categoryKeywords,
            // 臉部
            "防曬乳", "粉底液", "氣墊粉餅", "隔離乳", "遮瑕", "粉餅", 
            "蜜粉", "打亮", "修容", "腮紅", "妝前乳", "散粉",
            // 唇部
            "口紅", "唇膏", "唇釉", "護唇膏", "唇蜜", "唇油", "唇線筆",
            // 眼部
            "眼影", "睫毛膏", "眼線液筆", "眼線膠筆", "眼線膏", "眉筆", 
            "染眉膏", "眉粉", "假睫毛", "假睫毛膠水", "睫毛夾", "臥蠶筆",
            // 其他工具 (根據 Prompt 歸類於此)
            "妝前保濕噴霧", "定妝噴霧", "化妝刷", "粉撲", "雙眼皮貼", 
            "美妝蛋", "夾睫毛器", "美妝海綿"
        );
    }

    // --- 2. 功能性關鍵字 (對應功效與情境 - 權重 0.25) ---
    private void initializeFunctionalKeywords() {
        functionalKeywords = new HashSet<>();
        Collections.addAll(functionalKeywords, 
            // 功效與質地
            "深層保濕", "補水鎖水", "長效滋潤", "水感光澤", "提亮膚色", 
            "均勻膚色", "改善蠟黃", "去暗沉", "珠光亮肌", "修復屏障", 
            "淡斑", "抗皺", "抗老修復", "抑制出油", "持久控油", "持妝", 
            "防汗", "防水", "抗暈", "不沾杯", "修飾毛孔", "柔焦效果", 
            "霧面", "光澤", "奶油肌", "陶瓷肌", "高遮瑕", "透亮感",
            // 使用者族群
            "敏感肌", "乾肌", "油肌", "混合肌",
            // 季節與情境
            "夏季", "秋冬", "防曬", "控油", "保濕"
        );
    }

    // --- 3. 其他關鍵字 (輔助詞與品牌 - 權重 0.05) ---
    private void initializeOtherKeywords() {
        otherKeywords = new HashSet<>();
        
        // 3-1. 輔助詞
        Collections.addAll(otherKeywords,
            "刷具", "化妝棉", "卸妝水", "卸妝油", "卸妝乳", 
            "保養品", "a酸", "a醇", "皮膚科"
        );

        // 3-2. 品牌庫 (轉為小寫以利比對)
        // 歐美大牌
        Collections.addAll(otherKeywords,
            "dior", "迪奧", "chanel", "香奈兒", "ysl", "聖羅蘭", "m.a.c", "mac",
            "giorgio armani", "喬治亞曼尼", "lancôme", "lancome", "蘭蔻", 
            "tom ford", "湯姆福特", "estee lauder", "雅詩蘭黛", "guerlain", "嬌蘭", 
            "clinique", "倩碧", "bobbi brown", "nars", "givenchy", "burberry",
            "charlotte tilbury", "fenty beauty", "rare beauty", "huda beauty", 
            "benefit", "make up for ever", "urban decay", "too faced", "anastasia",
            "laura mercier", "hourglass", "stila", "pat mcgrath", "kvd"
        );

        // 韓系
        Collections.addAll(otherKeywords,
            "rom&nd", "romand", "the face shop", "clio", "珂莉奧", "etude", 
            "a’pieu", "laneige", "hera", "sulwhasoo", "dr. jart+", "innisfree", 
            "3ce", "peripera", "amuse", "dasique", "wakemake", "fwee", 
            "muzigae mansion", "laka", "hince", "iope", "mamonde", "missha", 
            "nature republic", "holika holika", "banila co.", "tonymoly", 
            "skinfood", "torriden", "round lab"
        );

        // 日系
        Collections.addAll(otherKeywords,
            "shiseido", "資生堂", "sk-ii", "kanebo", "kosé", "高絲", "雪肌精", 
            "suqqu", "rmk", "three", "albion", "elegance", "ipsa", "orbis", 
            "pola", "hada labo", "肌研", "curel", "珂潤", "shu uemura", "植村秀", 
            "maquillage", "integrate", "ettusais", "canmake", "kate", "cezanne", 
            "dhc", "melano cc", "shiro", "excel", "kiss me"
        );

        // 台灣
        Collections.addAll(otherKeywords,
            "heme", "1028", "solone", "unt", "miss hana", "fresho2", 
            "unicat", "小奶貓", "pony effect", "hicos", "mkup", 
            "a.m fairyland", "林三益"
        );
        
        // 泰國與中國品牌
        Collections.addAll(otherKeywords,
            "perfect diary", "完美日記", "florasis", "花西子", "judydoll", "橘朵", 
            "marie dalgar", "瑪麗黛佳", "colorkey", "zeesea", "滋色",
            "srichand", "mistine", "4u2", "cute press", "karmart", "cathydoll"
        );
        
        // 平價歐美 (Drugstore)
        Collections.addAll(otherKeywords,
            "nyx", "e.l.f.", "colourpop", "l.a. girl", "wet n wild", 
            "smashbox", "rimmel", "maybelline", "loreal", "revlon", 
            "essence", "catrice", "kiko"
        );
    }

    /**
     * 掃描文章內容，統計各類關鍵字出現次數
     */
    public Map<String, Integer> extractKeywords(String content) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        if (content == null || content.isEmpty()) return frequencyMap;

        // 轉小寫以利英文品牌比對 (中文不受影響)
        String contentLower = content.toLowerCase();

        // 掃描三大類
        scanAndCount(contentLower, functionalKeywords, frequencyMap);
        scanAndCount(contentLower, categoryKeywords, frequencyMap);
        scanAndCount(contentLower, otherKeywords, frequencyMap);
        
        return frequencyMap;
    }

    private void scanAndCount(String content, Set<String> keywords, Map<String, Integer> map) {
        for (String k : keywords) {
            int count = 0;
            int idx = 0;
            // 確保關鍵字也轉小寫比對
            String kLower = k.toLowerCase();
            
            while ((idx = content.indexOf(kLower, idx)) != -1) {
                count++;
                idx += kLower.length();
            }
            if (count > 0) {
                map.put(k, count); // 這裡存回原本的 key 格式
            }
        }
    }

    // --- 提供給 Ranker 的判斷方法 ---

    public boolean isFunctionalKeyword(String word) {
        return functionalKeywords.contains(word) || functionalKeywords.contains(word.toLowerCase());
    }

    public boolean isCategoryKeyword(String word) {
        return categoryKeywords.contains(word) || categoryKeywords.contains(word.toLowerCase());
    }

    public boolean isOtherKeyword(String word) {
        return otherKeywords.contains(word) || otherKeywords.contains(word.toLowerCase());
    }
}