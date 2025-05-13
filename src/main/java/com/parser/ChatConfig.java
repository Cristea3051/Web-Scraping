package com.parser;

import java.util.List;

/**
 * Centralized configuration for Telegram chat IDs that should receive notifications.
 *
 * To retrieve a chat ID:
 * - On Windows (PowerShell):
 *     Invoke-RestMethod -Uri "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates"
 *
 * - On Linux/macOS (Terminal):
 *     curl -s "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates"
 *
 * Replace <YOUR_BOT_TOKEN> with your actual bot token.
 */
public class ChatConfig {
    public static final List<Long> CHAT_IDS = List.of(
            1019028913L,
            5565832402L //Taniusa
            // Add additional chat IDs here as needed
    );
}
