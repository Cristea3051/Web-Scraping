package com.parser;

import com.microsoft.playwright.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

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
        // Initialize bot
        ParserSTVBot bot = new ParserSTVBot();
        long chatId = 1019028913;

        // Register bot
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Bot registered successfully");
        } catch (TelegramApiException e) {
            System.err.println("Failed to register bot: " + e.getMessage());
            return; // Stop execution if bot registration fails
        }

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
                bot.sendMessage(chatId, "✅ Cookie modal was found and clicked.");
            } else {
                bot.sendMessage(chatId, "ℹ️ No cookie modal found. Skipping click.");
            }

            Locator articleLink = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[1]");
            String linkText = articleLink.innerText();
            Locator articleDate = page.locator("//*[@id='alxposts-2']/ul/li[1]/div/p[2]");
            String linkDate = articleDate.innerText();

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = today.format(formatter);
            if (linkDate.equals(formattedDate)) {
                bot.sendMessage(chatId, "Today: " + formattedDate);
                bot.sendMessage(chatId, "✅ Found article: " + linkText + " from date " + linkDate);
            } else {
                bot.sendMessage(chatId, "ℹ️ No content from today");
            }
            page.waitForTimeout(3000);
            context.close();
        }
    }
}

class ParserSTVBot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "ParserSTVBOT";
    }

    @Override
    public String getBotToken() {
        return "7863658607:AAFerJYvK8kq2Z8RVLKlfjBBDTusyqw2-uQ"; // Fallback direct token
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Not used here
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
            System.out.println("Sent message: " + text);
        } catch (TelegramApiException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
}