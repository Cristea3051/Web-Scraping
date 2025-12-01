package com.parser.scrapers;

import com.microsoft.playwright.Page;
import com.parser.ArticleScraper;
import com.parser.botconfig.TelegramBotConfig;
import com.microsoft.playwright.*;
import com.parser.db.DBHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class OnipmScraper implements ArticleScraper {
    private static final String URL = "https://onipm.gov.md/news?field_press_release_type_tid=All";

    @Override
    public String getUrl() {
        return URL;
    }

    @Override
    public void checkLatestArticles(TelegramBotConfig botConfig, Page page) {
        List<Locator> titles = page.locator("h4.views-field-title span a").all();
        List<Locator> dates = page.locator("//div[contains(@class, 'views-row')]//span[not(a)]").all();

        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate targetDate = LocalDate.now();

        boolean found = false;

        for (int i = 0; i < Math.min(titles.size(), dates.size()); i++) {
            String titleText = titles.get(i).innerText().trim();
            String dateText = dates.get(i).innerText().trim();
            String linkHref = titles.get(i).getAttribute("href");

            try {
                LocalDate articleDate = LocalDate.parse(dateText, siteFormatter);

                if (articleDate.equals(targetDate)) {
                    try (Connection conn = DBHelper.getConnection()) {
                        if (DBHelper.articleExists(conn, titleText, linkHref)) {
                            DBHelper.insertArticle(conn, titleText, linkHref);
                            botConfig.sendToAll("✅ Found article:\n" + titleText + " (" + dateText + ")\n" + URL);
                            found = true;
                        } else {
                            System.out.println("⚠️ Articolul există deja în DB.");
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Eroare DB: " + e.getMessage());
                    }
                }
            } catch (DateTimeParseException e) {
                System.out.printf("❌ Nu s-a putut parsa data: %s (%s)%n", dateText, URL);
            }
        }

        if (!found) {
            System.out.println("❌ No content from date: " + targetDate.format(siteFormatter));
        }
    }

}