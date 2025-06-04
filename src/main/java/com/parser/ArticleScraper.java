package com.parser;

import com.microsoft.playwright.Page;

public interface ArticleScraper {
    void checkLatestArticles(TelegramBotConfig botConfig, Page page);
    String getUrl();
}