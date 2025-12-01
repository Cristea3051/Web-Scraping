package com.parser.scrapers;

import com.parser.ArticleScraper;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.*;
import com.parser.botconfig.TelegramBotConfig;
import com.parser.db.DBHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CivicScraper implements ArticleScraper {

    private static final String URL = "https://civic.md/anunturi/granturi.html";

    @Override
    public String getUrl() {
        return URL;
    }

    @Override
    public void checkLatestArticles(TelegramBotConfig botConfig, Page page) {
        List<Locator> titles = page.locator("td.list-title a").all();
        List<Locator> dates = page.locator("td.list-date").all();

        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate targetDate = LocalDate.now();

        boolean found = false;

        for (int i = 0; i < Math.min(titles.size(), dates.size()); i++) {
            String titleText = titles.get(i).innerText().trim();
            String linkHrefRelative = titles.get(i).getAttribute("href");
            String linkHref = "https://civic.md" + linkHrefRelative;

            String dateTextRaw = dates.get(i).innerText().trim();

            try {
                LocalDate articleDate = LocalDate.parse(dateTextRaw, siteFormatter);

                if (articleDate.equals(targetDate)) {
                    try (Connection conn = DBHelper.getConnection()) {
                        if (DBHelper.articleExists(conn, titleText, linkHref)) {
                            DBHelper.insertArticle(conn, titleText, linkHref);
                            botConfig.sendToAll("✅ Found article:\n" + titleText + " (" + dateTextRaw + ")\n" + linkHref);
                            found = true;
                        } else {
                            System.out.println("⚠️ Articolul există deja în DB.");
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Eroare DB: " + e.getMessage());
                    }
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateTextRaw + " (" + URL + ")");
            }
        }

        if (!found) {
            System.out.println("❌ No content from date: " + targetDate.format(siteFormatter) + " (" + URL + ")");
        }
    }
}