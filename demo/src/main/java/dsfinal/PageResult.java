package dsfinal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageResult {
    public String title;
    public String content;
    public String url;
    public List<String> subLinks;
    
    public List<PageResult> childPages; 
    public double score; 

    // 儲存關鍵字統計
    public Map<String, Integer> keywords;

    public PageResult(String title, String content, String url, List<String> subLinks) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.subLinks = subLinks;
        this.childPages = new ArrayList<>();
        this.score = 0.0;
        
        //初始化為空 Map
        this.keywords = new HashMap<>();
    }
    
    public void addChildPage(PageResult child) {
        this.childPages.add(child);
    }
}