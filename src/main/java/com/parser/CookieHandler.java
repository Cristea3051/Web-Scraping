package com.parser;

import com.microsoft.playwright.*;

public class CookieHandler {
    public static void handleCookieModal(TelegramBotConfig botConfig, Page page) {
        ElementHandle cookieButton = page.querySelector("#wt-cli-accept-all-btn");
        if (cookieButton != null && cookieButton.isVisible()) {
            cookieButton.click();
            botConfig.sendToAll("✅ Cookie modal was found and clicked.");
        }
    }
}
