package com.parser.scrapers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.parser.ArticleScraper;
import com.parser.botconfig.TelegramBotConfig;
import com.parser.db.DBHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class AIPAScraper implements ArticleScraper {

    private static final String URL = "http://aipa.gov.md/media/comunicate/";

    @Override
    public String getUrl() {
        return URL;
    }

    @Override
    public void checkLatestArticles(TelegramBotConfig botConfig, Page page) {
        // Selectăm toate linkurile din titluri
        List<Locator> titles = page.locator("div.pcsl-title a").all();
        // Selectăm toate datele
        List<Locator> dates = page.locator("time.entry-date.published").all();

        // Formatul datei de pe site este "d MMMM yyyy" în română (ex: 4 noiembrie 2025)
        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ro"));
        LocalDate targetDate = LocalDate.now();

        boolean found = false;

        for (int i = 0; i < Math.min(titles.size(), dates.size()); i++) {
            String titleText = titles.get(i).innerText().trim();
            // Link-ul este deja absolut
            String linkHref = titles.get(i).getAttribute("href");

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
