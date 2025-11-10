package com.example.dsfinal;

public class SearchResult {
    private String title;
    private String link;

    public SearchResult(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() { return title; }
    public String getLink() { return link; }
}
