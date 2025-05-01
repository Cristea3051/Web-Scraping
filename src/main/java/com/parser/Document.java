package com.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Document {
    @JsonProperty("title")
    private String title;
    @JsonProperty("link")
    private String link;
    @JsonProperty("date")
    private String date;

    public Document(String title, String link, String date) {
        this.title = title;
        this.link = link;
        this.date = date;
    }

    // Getters, setters, equals, hashCode (generate using IDE)
    @Override
    public String toString() {
        return "Document{title='" + title + "', link='" + link + "', date='" + date + "'}";
    }
}
