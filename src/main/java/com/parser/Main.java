package com.parser;

import com.microsoft.playwright.*;
import com.parser.botconfig.TelegramBotConfig;
import com.parser.db.DBHelper;
import com.parser.scrapers.EgrantScraper;
import com.parser.scrapers.MidrScraper;
import com.parser.scrapers.OndrlScraper;
import com.parser.scrapers.OnipmScraper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        TelegramBotConfig botConfig = new TelegramBotConfig();
        if (!botConfig.registerBot()) {
            LOGGER.severe("Bot registration failed. Exiting...");
            System.exit(1);
        }

        try {
            scrapeAndNotifyAll(botConfig);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during scraping", e);
            botConfig.sendToAll("❌ An unexpected error occurred: " + e.getMessage());
        } finally {
            LOGGER.info("Shutting down application...");
            System.exit(0);
        }
    }

    public static void scrapeAndNotifyAll(TelegramBotConfig botConfig) {
        try (Connection conn = DBHelper.getConnection()) {
            DBHelper.deleteOldArticles(conn);
        } catch (SQLException e) {
            System.out.println("❌ Eroare la ștergerea articolelor vechi: " + e.getMessage());
        }
        try (Playwright playwright = Playwright.create()) {
            BrowserContext context = setupBrowserContext(playwright);
            try (context) {
                Page page = context.pages().getFirst();

                List<ArticleScraper> scrapers = List.of(
                        new OndrlScraper(),
                        new EgrantScraper(),
                        new MidrScraper(),
                        new OnipmScraper()
                );

                for (ArticleScraper scraper : scrapers) {
                    page.navigate(scraper.getUrl());
                    page.waitForTimeout(3000);
                    scraper.checkLatestArticles(botConfig, page);
                }
            }
        }
    }

    private static BrowserContext setupBrowserContext(Playwright playwright) {
        List<String> arguments = new ArrayList<>();
        arguments.add("--start-maximized");

        Path userDataDir = Paths.get("my-user-data-dir");
        BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(true)
                .setArgs(arguments)
                .setViewportSize(null);

        return playwright.chromium().launchPersistentContext(userDataDir, options);
    }
}