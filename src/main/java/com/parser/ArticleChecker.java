package com.parser;

import com.microsoft.playwright.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ArticleChecker {
    public static void checkLatestArticle(TelegramBotConfig botConfig, Page page) {
        Locator articleLink = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[1]");
        String linkText = articleLink.innerText();
        Locator articleDate = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[2]");
        String linkDate = articleDate.innerText();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);

        if (linkDate.equals(formattedDate)) {
            botConfig.sendToAll("Today: " + formattedDate);
            botConfig.sendToAll("✅ Found article: " + linkText + " from date " + linkDate);
        } else {
            botConfig.sendToAll("❌ No content from today");
        }
    }

    public static void checkLatestArticleFromOndrl(TelegramBotConfig botConfig, Page page) {
        Locator articleLink = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[1]");
        String linkText = articleLink.innerText();
        Locator articleDate = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[2]");
        String linkDate = articleDate.innerText();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = today.format(formatter);

        if (linkDate.equals(formattedDate)) {
            botConfig.sendToAll("Today: " + formattedDate);
            botConfig.sendToAll("✅ Found article: " + linkText + " from date " + linkDate);
        } else {
            botConfig.sendToAll("❌ No content from today");
        }
    }
}
