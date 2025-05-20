package com.parser;

import com.microsoft.playwright.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class ArticleChecker {
    public static void checkLatestArticles(TelegramBotConfig botConfig, Page page) {

        List<Locator> titles = page.locator("//*[@id='alxposts-2']/ul/li/div/p[1]/a").all();
        List<Locator> dates = page.locator("//*[@id='alxposts-2']/ul/li/div/p[2]").all();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

//        LocalDate targetDate = LocalDate.parse("08/05/2025", formatter);

         LocalDate targetDate = LocalDate.now();

        String formattedTargetDate = targetDate.format(formatter);
        boolean found = false;

        for (int i = 0; i < Math.min(titles.size(), dates.size()); i++) {
            String titleText = titles.get(i).innerText().trim();
            String dateText = dates.get(i).innerText().trim();
            String linkHref = titles.get(i).getAttribute("href");

            try {
                LocalDate articleDate = LocalDate.parse(dateText, formatter);

                if (articleDate.equals(targetDate)) {
                    found = true;
                    botConfig.sendToAll("✅ Found article: " + titleText + " (" + dateText + ")" + linkHref);
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateText);
            }
        }

        if (!found) {
            botConfig.sendToAll("❌ No content from date (" + formattedTargetDate + ")");
        }
    }

    public static void checkLatestArticlesFromOndrl(TelegramBotConfig botConfig, Page page) {
        List<Locator> titles = page.locator("//h3[@class='entry-title td-module-title']/a").all();
        List<Locator> dates = page.locator("//span[@class='td-post-date']/time").all();

        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ro"));

//        LocalDate targetDate = LocalDate.parse("8 mai 2025", siteFormatter);

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
                    botConfig.sendToAll("✅ Found article: " + titleText + " (" + dateText + ")" + linkHref);
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateText);
            }
        }

        if (!found) {
            botConfig.sendToAll("❌ No content from date: " + targetDate.format(siteFormatter));
        }
    }

    public static void chechLatestArticlesFromEgrant(TelegramBotConfig botConfig, Page page){
        List<Locator> titles = page.locator("h2.entry-title a").all();
        List<Locator> dates = page.locator("time.entry-date").all();

        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("MMMM d yyyy", new Locale("ro"));

//         LocalDate targetDate = LocalDate.parse("aprilie 14 2025", siteFormatter);
        LocalDate targetDate = LocalDate.now();

        boolean found = false;

        for (int i = 0; i < Math.min(titles.size(), dates.size()); i++) {
            String titleText = titles.get(i).innerText().trim();
            String linkHref = titles.get(i).getAttribute("href");

            String dateTextRaw = dates.get(i).innerText().trim();

            // ✅ Curățăm data: minuscul și fără virgulă
            String dateText = dateTextRaw.toLowerCase().replace(",", "");

            try {
                LocalDate articleDate = LocalDate.parse(dateText, siteFormatter);

                if (articleDate.equals(targetDate)) {
                    found = true;
                    botConfig.sendToAll("✅ Found article: " + titleText + " (" + dateTextRaw + ") " + linkHref);
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateTextRaw);
            }
        }

        if (!found) {
            botConfig.sendToAll("❌ No content from date: " + targetDate.format(siteFormatter));
        }
    }

    public static void checkLatestArticlesFromMidr(TelegramBotConfig botConfig, Page page) {
        List<Locator> articles = page.locator("div.description-latest a h2, div.description-latest a h3").all();
        List<Locator> links = page.locator("div.description-latest a:nth-child(1)").all();
        List<Locator> dates = page.locator("span.text-uppercase.date").all();

        DateTimeFormatter siteFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", new Locale("ro"));

        // Target date deja formatată corect
        LocalDate targetDate = LocalDate.parse("mai 19, 2025", siteFormatter);

        boolean found = false;

        for (int i = 0; i < Math.min(links.size(), dates.size()); i++) {
            String linkHref = links.get(i).getAttribute("href");
            String titleText = articles.get(i).innerText().trim();

            String dateTextRaw = dates.get(i).innerText().trim();

            try {
                LocalDate articleDate = LocalDate.parse(dateTextRaw, siteFormatter);

                if (articleDate.equals(targetDate)) {
                    found = true;
                    botConfig.sendToAll("✅ Found article:  \n" + titleText + " (" + dateTextRaw + ") " + linkHref);
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Nu s-a putut parsa data: " + dateTextRaw);
            }
        }

        if (!found) {
            botConfig.sendToAll("❌ No content from date: " + targetDate.format(siteFormatter));
        }
    }

    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }



}
