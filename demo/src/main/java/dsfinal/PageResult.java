package dsfinal;

import java.util.List;

public class PageResult {
    public String title;
    public String content;
    public String url;
    public List<String> subLinks;
    // 之後 ProductRanker 會用到這個分數
    public double score; 

    public PageResult(String title, String content, String url, List<String> subLinks) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.subLinks = subLinks;
        this.score = 0.0;
    }
}