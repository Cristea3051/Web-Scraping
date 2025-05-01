package com.parser;


public class SiteConfig {
    private final String name; // Numele site-ului (ex. "DPRP")
    private final String url; // URL-ul paginii
    private final String linkSelector; // Selector CSS pentru link-uri
    private final String filePath; // Calea fișierului JSON pentru stocare

    public SiteConfig(String name, String url, String linkSelector, String filePath) {
        this.name = name;
        this.url = url;
        this.linkStatistics = linkSelector;
        this.filePath = filePath;
    }

    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getLinkSelector() { return linkSelector; }
    public String getFilePath() { return filePath; }
}