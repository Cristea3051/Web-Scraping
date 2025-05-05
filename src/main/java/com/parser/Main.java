package com.parser;

import com.microsoft.playwright.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
                            .setViewportSize(null); // IMPORTANT

            BrowserContext context = playwright.chromium().launchPersistentContext(userDataDir, options);
            Page page = context.pages().get(0);
            page.navigate("https://dprp.gov.ro/web/rezultate-sesiune-de-finantare-2020/");
            page.waitForTimeout(3000);
            ElementHandle cookieButton = page.querySelector("#wt-cli-accept-all-btn");

            if (cookieButton != null && cookieButton.isVisible()) {
                cookieButton.click();
                System.out.println("✅ Cookie modal was found and clicked.");
            } else {
                System.out.println("ℹ️ No cookie modal found. Skipping click.");
            }
            page.waitForTimeout(3000);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
            context.close();
        }
    }

}
