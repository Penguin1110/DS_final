package dsfinal;

import java.util.ArrayList;
import java.util.List;

public class PageResult {
    public String title;
    public String content;
    public String url;
    public List<String> subLinks; // 網址列表
    
    // ✅ 新增：儲存實際抓下來的子頁面物件 (用於遞迴分數計算)
    public List<PageResult> childPages; 
    
    public double score; 

    public PageResult(String title, String content, String url, List<String> subLinks) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.subLinks = subLinks;
        this.childPages = new ArrayList<>();
        this.score = 0.0;
    }
    
    public void addChildPage(PageResult child) {
        this.childPages.add(child);
    }
}