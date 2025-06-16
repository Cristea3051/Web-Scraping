package com.parser;

import com.microsoft.playwright.Page;
import com.parser.botconfig.TelegramBotConfig;

public interface ArticleScraper {
    void checkLatestArticles(TelegramBotConfig botConfig, Page page);
    String getUrl();
}