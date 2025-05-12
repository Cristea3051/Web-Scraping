package com.parser;

import com.microsoft.playwright.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BrowserContextFactory {
    public static BrowserContext createContext(Playwright playwright) {
        List<String> arguments = new ArrayList<>();
        arguments.add("--start-maximized");

        Path userDataDir = Paths.get("my-user-data-dir");
        BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(false)
                .setArgs(arguments)
                .setViewportSize(null);

        return playwright.chromium().launchPersistentContext(userDataDir, options);
    }
}
