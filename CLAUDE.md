# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java-based web scraping application that monitors government websites for new articles and sends notifications via Telegram. It uses Playwright for browser automation and PostgreSQL for article deduplication.

## Build and Run Commands

### Build
```bash
mvn clean compile
mvn clean package
```

### Run Application
```bash
mvn exec:java
```
Main class: `com.parser.Main`

### Install Playwright Dependencies
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install-deps"
```

### Run Tests
```bash
mvn test
```

### Build without Tests
```bash
mvn -B install -D skipTests --no-transfer-progress
```

## Architecture

### Core Components

**Main Entry Point** (`Main.java`)
- Initializes Telegram bot configuration
- Sets up Playwright browser context with persistent session
- Orchestrates scraper execution in sequence
- Cleans up old database articles (30+ days) before each run

**Scraper Pattern** (`ArticleScraper` interface)
- All scrapers implement this interface with two methods:
  - `checkLatestArticles(TelegramBotConfig, Page)`: Scrapes and processes articles
  - `getUrl()`: Returns the target URL
- Current implementations: `OnipmScraper`, `OndrlScraper`, `EgrantScraper`, `MidrScraper`

**Database Layer** (`DBHelper.java`)
- Uses PostgreSQL for article storage
- Connection URL loaded from `.env` file via dotenv-java
- Key operations:
  - `articleExists()`: Checks for duplicates by title and URL
  - `insertArticle()`: Stores new articles with timestamp
  - `deleteOldArticles()`: Auto-cleanup of articles older than 30 days

**Telegram Integration** (`TelegramBotConfig.java`, `ChatConfig.java`)
- Bot credentials hardcoded in `TelegramBotConfig` (BOT_TOKEN, BOT_USERNAME)
- `ChatConfig.CHAT_IDS`: Static list of recipient chat IDs
- `sendToAll()`: Broadcasts messages to all configured chats
- Bot registration must succeed or application exits

### Scraper Workflow

1. Navigate to target URL
2. Wait 3 seconds for page load
3. Extract article titles, dates, and links using Playwright locators
4. Parse dates and compare with current date
5. For matching articles:
   - Check if article exists in database
   - If new, insert into DB and send Telegram notification
   - If duplicate, skip silently

### Environment Configuration

**.env file** (required):
```
DB_URL=jdbc:postgresql://[host]/[database]?user=[user]&password=[password]&sslmode=require
```

### CI/CD

GitHub Actions workflow runs on:
- Every 3 hours (cron schedule)
- Push to main/master branches
- Pull requests

The workflow installs Playwright dependencies but note that the final step only installs Playwright CLI, not the actual application execution.

## Key Technologies

- Java 21
- Maven (build tool)
- Playwright (browser automation)
- PostgreSQL (article storage)
- Telegram Bot API (notifications)
- dotenv-java (environment variables)

## Adding New Scrapers

1. Create a class in `com.parser.scrapers` package
2. Implement `ArticleScraper` interface
3. Use Playwright locators to extract article data
4. Call `DBHelper.articleExists()` and `DBHelper.insertArticle()` for deduplication
5. Register in `Main.scrapeAndNotifyAll()` scrapers list

## Database Schema

**articles** table columns:
- `title` (String)
- `url` (String)
- `inserted_at` (Timestamp)

Compound uniqueness check on title + url.
