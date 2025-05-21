package com.parser;

import com.microsoft.playwright.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String TARGET_URL_ONDRL = "https://ondrl.gov.md/comunicare-publica/ ";
    private static final String URL_EGRANT = "https://egrant.md/category/granturi/";
    private static final String URL_MIDR = "https://midr.gov.md/ro/noutati";



    public static void main(String[] args) {
        TelegramBotConfig botConfig = new TelegramBotConfig();
        if (!botConfig.registerBot()) {
            LOGGER.severe("Bot registration failed. Exiting...");
            System.exit(1);
        }

        try {
            scrapeAndNotifiFromOndrl(botConfig);
            scrapeAndNotifiFromEgrant(botConfig);
            scrapeAndNotifiFromMidr(botConfig);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during scraping", e);
            botConfig.sendToAll("❌ An unexpected error occurred: " + e.getMessage());
        } finally {
            LOGGER.info("Shutting down application...");
            System.exit(0);
        }
    }

    public static void scrapeAndNotifiFromOndrl(TelegramBotConfig botConfig){

        try (Playwright playwright = Playwright.create()) {
            BrowserContext context = setupBrowserContext(playwright);
            try (context) {
                Page page = context.pages().getFirst();
                page.navigate(TARGET_URL_ONDRL);
                page.waitForTimeout(3000);
                ArticleChecker.checkLatestArticlesFromOndrl(botConfig, page);
            }
        }
    }

    public static void scrapeAndNotifiFromMidr(TelegramBotConfig botConfig){

        try (Playwright playwright = Playwright.create()) {
            BrowserContext context = setupBrowserContext(playwright);
            try (context) {
                Page page = context.pages().getFirst();
                page.navigate(URL_MIDR);
                page.waitForTimeout(3000);
                ArticleChecker.checkLatestArticlesFromMidr(botConfig, page);
            }
        }
    }

    public static void scrapeAndNotifiFromEgrant(TelegramBotConfig botConfig){

        try (Playwright playwright = Playwright.create()) {
            BrowserContext context = setupBrowserContext(playwright);
            try (context) {
                Page page = context.pages().getFirst();
                page.navigate(URL_EGRANT);
                page.waitForTimeout(3000);
                ArticleChecker.chechLatestArticlesFromEgrant(botConfig, page);
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
