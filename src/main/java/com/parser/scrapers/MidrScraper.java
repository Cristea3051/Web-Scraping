package com.parser.scrapers;

import com.microsoft.playwright.Page;
import com.parser.ArticleScraper;
import com.parser.botconfig.TelegramBotConfig;
import com.parser.db.DBHelper;
import com.microsoft.playwright.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class MidrScraper implements ArticleScraper {

    private static final String URL = "https://midr.gov.md/ro/noutati";

    @Override
    public String getUrl() {
        return URL;
    }
    @Override
    public void checkLatestArticles(TelegramBotConfig botConfig, Page page) {
        List<Locator> articles = page.locator("div.description-latest a h2, div.description-latest a h3").all();
        List<Locator> links = page.locator("div.description-latest a:nth-child(1)").all();
        List<Locator> dates = page.locator("span.text-uppercase.date").all();

        LocalDate targetDate = LocalDate.now();

        boolean found = false;

        for (int i = 0; i < Math.min(links.size(), dates.size()); i++) {
            String linkHref = links.get(i).getAttribute("href");
            String titleText = articles.get(i).innerText().trim();
            String dateTextRaw = dates.get(i).innerText().trim();

            try {
                LocalDate articleDate = parseRomanianDate(dateTextRaw);

                if (articleDate.equals(targetDate)) {
                    try (Connection conn = DBHelper.getConnection()) {
                        if (!DBHelper.articleExists(conn, titleText, linkHref)) {
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
            } catch (Exception e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateTextRaw + URL);
            }
        }

        if (!found) {
            System.out.println("❌ No content from date: " + targetDate);
        }
    }

    private static LocalDate parseRomanianDate(String rawDate) {
        rawDate = rawDate.toUpperCase(Locale.ROOT).replace(",", "");
        String[] parts = rawDate.split(" ");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Format invalid: " + rawDate);
        }

        int luna = mapLunaToNumar(parts[0]);
        int zi = Integer.parseInt(parts[1]);
        int an = Integer.parseInt(parts[2]);

        return LocalDate.of(an, luna, zi);
    }

    private static int mapLunaToNumar(String luna) {
        switch (luna) {
            case "IANUARIE": return 1;
            case "FEBRUARIE": return 2;
            case "MARTIE": return 3;
            case "APRILIE": return 4;
            case "MAI": return 5;
            case "IUNIE": return 6;
            case "IULIE": return 7;
            case "AUGUST": return 8;
            case "SEPTEMBRIE": return 9;
            case "OCTOMBRIE": return 10;
            case "NOIEMBRIE": return 11;
            case "DECEMBRIE": return 12;
            default: throw new IllegalArgumentException("Lună necunoscută: " + luna);
        }
    }
}