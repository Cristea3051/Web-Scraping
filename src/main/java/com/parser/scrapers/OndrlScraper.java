package com.parser.scrapers;

import com.microsoft.playwright.*;
import com.parser.ArticleScraper;
import com.parser.TelegramBotConfig;
import com.parser.db.DBHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class OndrlScraper implements ArticleScraper {

    @Override
    public void checkLatestArticles(TelegramBotConfig botConfig, Page page) {
        List<Locator> titles = page.locator("//h3[@class='entry-title td-module-title']/a").all();
        List<Locator> dates = page.locator("//span[@class='td-post-date']/time").all();

        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ro"));
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
                        if (!DBHelper.articleExists(conn, titleText, linkHref)) {
                            DBHelper.insertArticle(conn, titleText, linkHref);
                            botConfig.sendToAll("✅ Found article:\n" + titleText + " (" + dateText + ")\n" + linkHref);
                            found = true;
                        } else {
                            System.out.println("⚠️ Articolul există deja în DB.");
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Eroare DB: " + e.getMessage());
                    }
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateText);
            }
        }

        if (!found) {
            System.out.println("❌ No content from date: " + targetDate.format(siteFormatter));
        }
    }
}
