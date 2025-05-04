package com.parser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            List<String> arguments = new ArrayList<>();
            arguments.add("--start-maximized");
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setArgs(arguments));
            Page page = browser.newPage();
            page.navigate("https://dprp.gov.ro/web/rezultate-sesiune-de-finantare-2020/");
            page.waitForTimeout(10_000);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
        }
    }
}
