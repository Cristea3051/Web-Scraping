package com.parser;

import com.microsoft.playwright.Page;
import com.parser.scrapers.EgrantScraper;
import com.parser.scrapers.OndrlScraper;
import com.parser.scrapers.MidrScraper;

import java.util.Arrays;
import java.util.List;

public class ArticleChecker {

    public static void checkLatestArticles(TelegramBotConfig botConfig, Page page) {
        List<ArticleScraper> scrapers = Arrays.asList(
                new EgrantScraper(),
                new OndrlScraper(),
                new MidrScraper()
        );

        for (ArticleScraper scraper : scrapers) {
            scraper.checkLatestArticles(botConfig, page);
        }
    }
}
