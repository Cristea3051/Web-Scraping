package com.parser;

import com.microsoft.playwright.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class ArticleChecker {

    public static void checkLatestArticlesFromOndrl(TelegramBotConfig botConfig, Page page) {
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
                    found = true;
                    botConfig.sendToAll("✅ Found article:  \n" + titleText + " (" + dateText + ") \n" + linkHref);
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateText);
            }
        }

        if (!found) {
            System.out.println("❌ No content from date: " + targetDate.format(siteFormatter));
        }
    }

    public static void chechLatestArticlesFromEgrant(TelegramBotConfig botConfig, Page page){
        List<Locator> titles = page.locator("h2.entry-title a").all();
        List<Locator> dates = page.locator("time.entry-date").all();

        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("MMMM d yyyy", new Locale("ro"));

        LocalDate targetDate = LocalDate.now();

        boolean found = false;

        for (int i = 0; i < Math.min(titles.size(), dates.size()); i++) {
            String titleText = titles.get(i).innerText().trim();
            String linkHref = titles.get(i).getAttribute("href");

            String dateTextRaw = dates.get(i).innerText().trim();

            String dateText = dateTextRaw.toLowerCase().replace(",", "");

            try {
                LocalDate articleDate = LocalDate.parse(dateText, siteFormatter);

                if (articleDate.equals(targetDate)) {
                    found = true;
                    botConfig.sendToAll("✅ Found article:  \n" + titleText + " (" + dateTextRaw + ") \n" + linkHref);


                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateTextRaw);
            }
        }

        if (!found) {
            System.out.println("❌ No content from date: " + targetDate.format(siteFormatter));
        }
    }

    public static void checkLatestArticlesFromMidr(TelegramBotConfig botConfig, Page page) {
        List<Locator> articles = page.locator("div.description-latest a h2, div.description-latest a h3").all();
        List<Locator> links = page.locator("div.description-latest a:nth-child(1)").all();
        List<Locator> dates = page.locator("span.text-uppercase.date").all();

        LocalDate targetDate = LocalDate.now();

        System.out.println("Data de azi: " + targetDate);

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
