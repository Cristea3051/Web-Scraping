package com.parser.botconfig;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TelegramBotConfig extends TelegramLongPollingBot {
    private static final Logger LOGGER = Logger.getLogger(TelegramBotConfig.class.getName());
    private static final String BOT_USERNAME = "ParserSTVBOT";
    private static final String BOT_TOKEN = "7863658607:AAFerJYvK8kq2Z8RVLKlfjBBDTusyqw2-uQ";

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Not used in this implementation
    }

    public boolean registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            LOGGER.info("Bot registered successfully");
            return true;
        } catch (TelegramApiException e) {
            LOGGER.log(Level.SEVERE, "Failed to register bot: " + e.getMessage(), e);
            return false;
        }
    }
    public void sendToAll(String message) {
        for (long chatId : ChatConfig.CHAT_IDS) {
            sendMessage(chatId, message);
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
            LOGGER.info("Sent message: " + text);
        } catch (TelegramApiException e) {
            LOGGER.log(Level.SEVERE, "Failed to send message: " + e.getMessage(), e);
        }
    }
}
