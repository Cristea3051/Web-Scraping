package com.parser;

import com.microsoft.playwright.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String TARGET_URL = "https://dprp.gov.ro/web/rezultate-sesiune-de-finantare-2020/";
    private static final long CHAT_ID = 1019028913;

    public static void main(String[] args) {
        TelegramBotConfig botConfig = new TelegramBotConfig();
        if (!botConfig.registerBot()) {
            LOGGER.severe("Bot registration failed. Exiting...");
            System.exit(1);
        }

        try {
            scrapeAndNotify(botConfig);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during scraping", e);
            botConfig.sendMessage(CHAT_ID, "❌ An unexpected error occurred: " + e.getMessage());
        } finally {
            LOGGER.info("Shutting down application...");
            System.exit(0);
        }
    }

    private static void scrapeAndNotify(TelegramBotConfig botConfig) {
        try (Playwright playwright = Playwright.create()) {
            BrowserContext context = setupBrowserContext(playwright);
            try (context) {
                Page page = context.pages().get(0);
                page.navigate(TARGET_URL);
                page.waitForTimeout(3000);

                handleCookieModal(botConfig, page);
                checkLatestArticle(botConfig, page);
            }
        }
    }

    private static BrowserContext setupBrowserContext(Playwright playwright) {
        List<String> arguments = new ArrayList<>();
        arguments.add("--start-maximized");

        Path userDataDir = Paths.get("my-user-data-dir");
        BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(false)
                .setArgs(arguments)
                .setViewportSize(null);

        return playwright.chromium().launchPersistentContext(userDataDir, options);
    }

    private static void handleCookieModal(TelegramBotConfig botConfig, Page page) {
        ElementHandle cookieButton = page.querySelector("#wt-cli-accept-all-btn");
        if (cookieButton != null && cookieButton.isVisible()) {
            cookieButton.click();
            botConfig.sendMessage(CHAT_ID, "✅ Cookie modal was found and clicked.");
        } else {
            botConfig.sendMessage(CHAT_ID, "ℹ️ No cookie modal found. Skipping click.");
        }
    }

    private static void checkLatestArticle(TelegramBotConfig botConfig, Page page) {
        Locator articleLink = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[1]");
        String linkText = articleLink.innerText();
        Locator articleDate = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[2]");
        String linkDate = articleDate.innerText();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);

        if (linkDate.equals(formattedDate)) {
            botConfig.sendMessage(CHAT_ID, "Today: " + formattedDate);
            botConfig.sendMessage(CHAT_ID, "✅ Found article: " + linkText + " from date " + linkDate);
        } else {
            botConfig.sendMessage(CHAT_ID, "ℹ️ No content from today");
        }
    }
}