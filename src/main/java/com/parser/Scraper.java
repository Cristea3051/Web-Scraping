package com.parser;

import com.microsoft.playwright.*;
import java.util.ArrayList;
import java.util.List;

public class Scraper {
    public List<Document> scrape(SiteConfig config) {
        List<Document> documents = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate(config.getUrl());
            page.waitForLoadState();

            List<ElementHandle> links = page.querySelectorAll(config.getLinkSelector());

            for (ElementHandle link : links) {
                String title = link.innerText().trim();
                String href = link.getAttribute("href");
                String date = "N/A"; // Ajustează dacă site-ul oferă date

                if (title != null && !title.isEmpty() && href != null && href.endsWith(".pdf")) {
                    documents.add(new Document(title, href, date));
                }
            }

            browser.close();
        } catch (Exception e) {
            System.err.println("Eroare la scraping pe " + config.getName() + ": " + e.getMessage());
        }

        return documents;
    }
}