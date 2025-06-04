package com.parser.scrapers;

import com.microsoft.playwright.*;
import com.parser.ArticleScraper;
import com.parser.TelegramBotConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class MidrScraper implements ArticleScraper {

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
                    found = true;
                    botConfig.sendToAll("✅ Found article:  \n" + titleText + " (" + dateTextRaw + ") \n" + linkHref);
                }
            } catch (Exception e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateTextRaw);
            }
        }

        if (!found) {
            System.out.println("❌ No content from date: " + targetDate);
        }
    }

    private LocalDate parseRomanianDate(String rawDate) throws DateTimeParseException {
        // Exemple: "3 iunie 2024"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ro"));
        return LocalDate.parse(rawDate.toLowerCase(), formatter);
    }
}
