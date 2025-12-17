package dsfinal;

import java.util.*;

public class KeywordExtractor {
    private Set<String> functionalKeywords; // F: 功能性 (權重 0.25) - 包含功效、成分、膚質
    private Set<String> categoryKeywords;   // C: 分類詞 (權重視演算法而定) - 產品型態
    private Set<String> otherKeywords;      // O: 其他 (權重 0.05) - 品牌、輔助工具

    public KeywordExtractor() {
        initializeCategoryKeywords();
        initializeFunctionalKeywords();
        initializeOtherKeywords();
    }

    // --- 1. 分類關鍵字 (Product Categories) ---
    private void initializeCategoryKeywords() {
        categoryKeywords = new HashSet<>();
        Collections.addAll(categoryKeywords,
            // 底妝類
            "粉底", "粉底液", "粉底霜", "粉底棒", "氣墊", "氣墊粉餅", "粉餅", 
            "蜜粉", "蜜粉餅", "散粉", "定妝噴霧", "妝前乳", "隔離霜", "飾底乳", 
            "遮瑕", "遮瑕膏", "遮瑕液", "遮瑕盤", "校色", "BB霜", "CC霜", "素顏霜",

            // 臉部彩妝
            "腮紅", "腮紅液", "腮紅霜", "修容", "修容盤", "修容棒", 
            "打亮", "高光", "打亮盤", "打亮液", "鼻影",

            // 眼部彩妝
            "眼影", "眼影盤", "眼影蜜", "眼影筆", 
            "眼線", "眼線液", "眼線膠", "眼線筆", "眼線膏", 
            "睫毛膏", "睫毛底膏", "睫毛雨衣", 
            "眉筆", "眉粉", "眉膠", "染眉膏", "眉蠟",
            "臥蠶", "臥蠶筆", "雙眼皮貼", "假睫毛", "假睫毛膠",

            // 唇部彩妝
            "口紅", "唇膏", "唇釉", "唇萃", "唇泥", "唇露", "染唇液", 
            "唇蜜", "唇油", "護唇膏", "潤色護唇膏", "唇線筆", "豐唇蜜",

            // 保養類 (擴充)
            "化妝水", "精華液", "精華", "安瓶", "乳液", "乳霜", "凝凍", "凝膠",
            "眼霜", "眼部精華", "面膜", "凍膜", "泥膜", "晚安面膜",
            "去角質", "磨砂膏", "酸類", "化妝棉",
            "防曬", "防曬乳", "防曬凝膠", "防曬噴霧",

            // 清潔類
            "卸妝", "卸妝油", "卸妝水", "卸妝乳", "卸妝霜", "卸妝膏", "眼唇卸妝",
            "洗面乳", "潔顏乳", "洗面皂", "潔顏粉", "洗臉機"
        );
    }

    // --- 2. 功能性關鍵字 (Functional & Ingredients) - 權重 0.25 ---
    // 這裡包含了「成分」，因為成分通常代表了使用者想要的功能
    private void initializeFunctionalKeywords() {
        functionalKeywords = new HashSet<>();
        Collections.addAll(functionalKeywords, 
            // 基礎功效
            "保濕", "補水", "鎖水", "滋潤", "清爽", "不黏膩", "吸收快",
            "美白", "亮白", "去暗沉", "淡斑", "均勻膚色", "提亮", "改善蠟黃", "透明感",
            "控油", "吸油", "抑制出油", "油水平衡", "收斂",
            "抗老", "緊緻", "拉提", "撫紋", "抗皺", "澎潤", "膠原蛋白",
            "修復", "舒緩", "鎮定", "退紅", "抗敏", "修護屏障", "維穩",

            // 妝效與質地
            "持久", "不脫妝", "不沾杯", "不掉色", "持妝", "抗汗", "防水", "防暈",
            "遮瑕力", "高遮瑕", "修飾毛孔", "柔焦", "磨皮", "平滑",
            "霧面", "啞光", "絲絨", "光澤", "水光", "奶油肌", "陶瓷肌", "水潤", "鏡面",
            "顯色", "飽和", "清透", "薄透", "貼妝", "不卡粉", "不浮粉",

            // 問題膚質與需求
            "敏感肌", "乾肌", "油肌", "混合肌", "酒糟", "痘痘肌", "粉刺", "毛孔粗大",
            "黑眼圈", "淚溝", "法令紋", "細紋", "斑點", "泛紅",
            "溫和", "低敏", "無酒精", "無香精", "純素", "零殘忍", "不致粉刺", "不致痘",
            "醫美", "術後", "雷射",

            // 關鍵成分 (因為成分=功效，所以放這裡加分)
            "玻尿酸", "神經醯胺", "角鯊", "B5", "積雪草", "金盞花", "蘆薈",
            "維他命C", "早C晚A", "A醇", "A酸", "A醛", "視黃醇", "菸鹼醯胺", "B3",
            "水楊酸", "果酸", "杏仁酸", "杜鵑花酸", "傳明酸", "熊果素",
            "胜肽", "膠原蛋白", "Q10", "富勒烯", "酵母", "益生菌",
            "物理防曬", "化學防曬", "海洋友善"
        );
    }

    // --- 3. 其他關鍵字 (Other / Brands) - 權重 0.05 ---
    private void initializeOtherKeywords() {
        otherKeywords = new HashSet<>();
        
        // 3-1. 工具與輔助
        Collections.addAll(otherKeywords,
            "刷具", "粉底刷", "蜜粉刷", "腮紅刷", "眼影刷", "遮瑕刷", "唇刷",
            "美妝蛋", "粉撲", "氣墊粉撲", "海綿", "睫毛夾", "燙睫毛器",
            "收納", "化妝包", "鏡子"
        );

        // 3-2. 品牌庫 (全小寫，方便比對)
        initializeBrands();
    }

    private void initializeBrands() {
        // 歐美專櫃 (Luxury / Department Store)
        Collections.addAll(otherKeywords,
            "dior", "迪奧", "chanel", "香奈兒", "ysl", "聖羅蘭", "armani", "亞曼尼", "giorgio armani",
            "estee lauder", "雅詩蘭黛", "lancome", "lancôme", "蘭蔻", "la mer", "海洋拉娜",
            "sk-ii", "skii", "clarins", "克蘭詩", "shiseido", "資生堂", "cle de peau", "肌膚之鑰",
            "guerlain", "嬌蘭", "tom ford", "tf", "nars", "bobbi brown", "芭比波朗",
            "mac", "m.a.c", "clinique", "倩碧", "kiehls", "kiehl's", "契爾氏",
            "biotherm", "碧兒泉", "ipsa", "茵芙莎", "shu uemura", "植村秀", "kose", "高絲", "decorte", "黛珂",
            "kanebo", "佳麗寶", "pola", "suqqu", "rmk", "three", "albion", "艾倫比亞",
            "laura mercier", "蘿拉蜜思", "make up for ever", "makeupforever",
            "hr", "赫蓮娜", "valmont", "chantecaille", "香緹卡", "sisley", "希思黎",
            "darphin", "朵法", "aesop", "aveda", "jo malone", "diptyque", "byredo", "le labo",
            "charlotte tilbury", "ct", "pat mcgrath", "natasha denona", "hourglass",
            "urban decay", "too faced", "benefit", "anastasia", "fenty beauty", "huda beauty",
            "kvd", "stila", "tarte", "rare beauty", "glossier"
        );

        // 韓系 (K-Beauty)
        Collections.addAll(otherKeywords,
            "sulwhasoo", "雪花秀", "whoo", "后", "laneige", "蘭芝", "hera", "ioepe",
            "innisfree", "etude", "etude house", "clio", "珂莉奧", "peripera",
            "rom&nd", "romand", "3ce", "missha", "apieu", "a'pieu",
            "banila co", "too cool for school", "the face shop", "nature republic",
            "dr.jart+", "medicube", "cnp", "ahc", "torriden", "abib", "round lab",
            "ma:nyo", "魔女工廠", "goodal", "some by mi", "skin1004",
            "dasique", "hince", "laka", "wakemake", "amuse", "fwee", "muzigae mansion",
            "lilybyred", "bbia", "eglips", "kirsh", "alternativestereo"
        );

        // 日系開架 (J-Beauty Drugstore)
        Collections.addAll(otherKeywords,
            "kate", "media", "媚點", "visée", "visee", "excel", "canmake", "cezanne",
            "kiss me", "奇士美", "majolica", "戀愛魔鏡", "integrate", "櫻特芮",
            "za", "fasio", "sofina", "primavista", "anessa", "安耐曬", "allie",
            "biore", "蜜妮", "curel", "珂潤", "minon", "hadalabo", "肌研", "sana", "豆乳",
            "dr.wu", "dr.ci:labo", "orbis", "fancl", "dhc", "muji", "無印良品",
            "excel", "opera", "dejavu", "dup", "heroine make", "canmake"
        );

        // 台灣與其他 (Taiwan / Others)
        Collections.addAll(otherKeywords,
            "heme", "solone", "1028", "unt", "freshO2", "bevy c", "neogence", "霓淨思",
            "dr.wu", "達爾膚", "for beloved one", "寵愛之名", "timeless", "the ordinary",
            "cerave", "適樂膚", "laroche-posay", "理膚寶水", "vichy", "薇姿", "avene", "雅漾",
            "bioderma", "貝膚黛瑪", "nivea", "妮維雅", "neutrogena", "露得清", "loreal", "巴黎萊雅",
            "maybelline", "媚比琳", "revlon", "露華濃", "essence", "catrice", "wet n wild",
            "colourpop", "elf", "e.l.f.", "nyx", "rimmel", "bourjois", "妙巴黎",
            "perfect diary", "完美日記", "florasis", "花西子", "judydoll", "橘朵",
            "zeesea", "滋色", "colorkey", "into you", "mistine", "4u2", "srichand"
        );
    }

    // --- 掃描與判斷方法保持不變 ---
    
    public Map<String, Integer> extractKeywords(String content) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        if (content == null || content.isEmpty()) return frequencyMap;
        String contentLower = content.toLowerCase();

        scanAndCount(contentLower, functionalKeywords, frequencyMap);
        scanAndCount(contentLower, categoryKeywords, frequencyMap);
        scanAndCount(contentLower, otherKeywords, frequencyMap);
        
        return frequencyMap;
    }

    private void scanAndCount(String content, Set<String> keywords, Map<String, Integer> map) {
        for (String k : keywords) {
            int count = 0;
            int idx = 0;
            String kLower = k.toLowerCase();
            while ((idx = content.indexOf(kLower, idx)) != -1) {
                count++;
                idx += kLower.length();
            }
            if (count > 0) {
                map.put(k, count);
            }
        }
    }

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