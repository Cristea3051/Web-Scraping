package com.parser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50).setArgs(java.util.Arrays.asList("--start-maximized")));
            Page page = browser.newPage();
            page.navigate("https://dprp.gov.ro/web/rezultate-sesiune-de-finantare-2020/");
            page.waitForTimeout(60_000);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
        }
    }
}
