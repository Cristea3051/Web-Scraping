package com.parser;

import com.microsoft.playwright.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class Main {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            List<String> arguments = new ArrayList<>();
            arguments.add("--start-maximized");

            Path userDataDir = Paths.get("my-user-data-dir");

            BrowserType.LaunchPersistentContextOptions options =
                    new BrowserType.LaunchPersistentContextOptions()
                            .setHeadless(false)
                            .setArgs(arguments)
                            .setViewportSize(null);

            BrowserContext context = playwright.chromium().launchPersistentContext(userDataDir, options);
            Page page = context.pages().get(0);
            page.navigate("https://dprp.gov.ro/web/rezultate-sesiune-de-finantare-2020/");
            page.waitForTimeout(3000);

            ElementHandle cookieButton = page.querySelector("#wt-cli-accept-all-btn");
            if (cookieButton != null && cookieButton.isVisible()) {
                cookieButton.click();
                System.out.println(colorize("✅ Cookie modal was found and clicked.", CYAN_TEXT(), BOLD()));
            } else {
                System.out.println(colorize("ℹ️ No cookie modal found. Skipping click.", RED_TEXT(), BOLD()));
            }

            Locator postDate = page.locator("" +
                    "html > body > div:nth-of-type(1) > div > div > div > div > div:nth-of-type(1) > div > div:nth-of-type(5) > ul > li:nth-of-type(1) > div > p:nth-of-type(2)");
            if (postDate.isVisible()) {
                String dateText = postDate.innerText().trim();
                System.out.println(colorize(dateText, CYAN_TEXT(), BOLD()));
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate extractedDate = LocalDate.parse(dateText, formatter);
                    LocalDate referenceDate = LocalDate.of(2025, 4, 24); // 24.04.2025


                    if (extractedDate.equals(referenceDate)) {
                        Locator articleLink = page.locator("html > body > div:nth-of-type(1) > div > div > div > div > div:nth-of-type(1) > div > div:nth-of-type(5) > ul > li:nth-of-type(1) > div > p:nth-of-type(1)");
                        if (articleLink.isVisible()) {
                            String linkText = articleLink.innerText();
                            System.out.println(colorize("✅ Found article: " + linkText, CYAN_TEXT(), BOLD()));
                        } else {
                            System.out.println(colorize("❌ Article link not found.", RED_TEXT(), BOLD()));
                        }
                    } else {
                        System.out.println(colorize("ℹ️ Date does not match: " + dateText + " (expected 24.04.2025)", YELLOW_TEXT(), BOLD()));
                    }
                } catch (Exception e) {
                    System.out.println(colorize("❌ Failed to parse date: " + dateText, RED_TEXT(), BOLD()));
                }
            } else {
                System.out.println(colorize("❌ Post date not found.", RED_TEXT(), BOLD()));
            }

            page.waitForTimeout(3000);
            context.close();
        }
    }
}

