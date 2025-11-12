# Web Scraping & Notification System

An automated Java application that monitors government and grant websites for new articles and sends real-time notifications via Telegram. Built with Playwright for reliable web scraping and PostgreSQL for intelligent deduplication.

## Features

- **Automated Web Scraping**: Monitors multiple websites using Playwright browser automation
- **Real-time Notifications**: Instant Telegram alerts when new articles are published
- **Smart Deduplication**: PostgreSQL-backed article tracking prevents duplicate notifications
- **Auto-cleanup**: Automatically removes articles older than 30 days
- **Scheduled Execution**: GitHub Actions workflow runs every 3 hours
- **Multi-site Support**: Easily extensible scraper architecture for adding new websites

## Monitored Websites

Currently tracking the following sources:

1. **ONIPM** (National Intellectual Property Office) - https://onipm.gov.md/news
2. **ONDRL** (National Office for Registration of Rights) - https://ondrl.gov.md/comunicare-publica/
3. **eGrant** (Grant Information Portal) - https://egrant.md/category/granturi/
4. **MIDR** (Ministry of Infrastructure and Regional Development) - https://midr.gov.md/ro/noutati

## Technology Stack

- **Java 21** - Modern Java LTS version
- **Maven** - Dependency management and build tool
- **Playwright** - Browser automation framework
- **PostgreSQL** - Article storage and deduplication
- **Telegram Bot API** - Real-time notifications
- **dotenv-java** - Environment configuration
- **JUnit 5** - Testing framework

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database
- Telegram Bot (create one via [@BotFather](https://t.me/botfather))
- Internet connection for scraping

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Web-Scraping
```

### 2. Install Playwright Dependencies

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install-deps"
```

### 3. Configure Environment Variables

Create a `.env` file in the project root:

```env
DB_URL=jdbc:postgresql://[host]:[port]/[database]?user=[username]&password=[password]&sslmode=require
```

Example:
```env
DB_URL=jdbc:postgresql://localhost:5432/articles?user=myuser&password=mypass&sslmode=require
```

### 4. Configure Telegram Bot

Edit `src/main/java/com/parser/botconfig/TelegramBotConfig.java`:
- Replace `BOT_TOKEN` with your bot token
- Replace `BOT_USERNAME` with your bot username

### 5. Add Chat IDs

Edit `src/main/java/com/parser/botconfig/ChatConfig.java` and add chat IDs that should receive notifications.

**To get your chat ID:**

On Windows (PowerShell):
```powershell
Invoke-RestMethod -Uri "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates"
```

On Linux/macOS:
```bash
curl -s "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates"
```

### 6. Set Up Database

Create a PostgreSQL database and table:

```sql
CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    inserted_at TIMESTAMP NOT NULL,
    UNIQUE(title, url)
);
```

### 7. Build the Project

```bash
mvn clean compile
mvn clean package
```

## Usage

### Run the Application

```bash
mvn exec:java
```

The application will:
1. Initialize the Telegram bot
2. Clean up articles older than 30 days
3. Launch a headless Chromium browser
4. Visit each monitored website
5. Check for articles published today
6. Send Telegram notifications for new articles
7. Exit automatically

### Build Commands

```bash
# Compile only
mvn clean compile

# Run tests
mvn test

# Build JAR package
mvn clean package

# Install dependencies without tests
mvn -B install -D skipTests --no-transfer-progress
```

## Architecture

### Component Overview

```
Main.java
    ├── TelegramBotConfig (notification system)
    ├── DBHelper (database operations)
    └── ArticleScraper implementations
            ├── OnipmScraper
            ├── OndrlScraper
            ├── EgrantScraper
            └── MidrScraper
```

### Workflow

1. **Initialization**: Telegram bot registers with API
2. **Cleanup**: Old articles (30+ days) removed from database
3. **Browser Setup**: Playwright launches persistent Chromium context
4. **Scraping Loop**: For each scraper:
   - Navigate to target URL
   - Wait 3 seconds for page load
   - Extract article titles, dates, and links
   - Parse dates (handles multiple Romanian date formats)
   - Compare with today's date
   - Check database for duplicates
   - Insert new articles and send notifications
5. **Shutdown**: Browser closes and application exits

### Scraper Interface

All scrapers implement `ArticleScraper`:

```java
public interface ArticleScraper {
    void checkLatestArticles(TelegramBotConfig botConfig, Page page);
    String getUrl();
}
```

### Database Schema

**Table: articles**

| Column | Type | Description |
|--------|------|-------------|
| id | SERIAL | Primary key |
| title | VARCHAR(500) | Article title |
| url | VARCHAR(1000) | Article URL |
| inserted_at | TIMESTAMP | Insertion timestamp |

**Constraints:**
- Unique constraint on (title, url) for deduplication

### Date Parsing Strategies

Different websites use different date formats:

- **ONIPM**: `d/M/yyyy` (e.g., "4/6/2025")
- **ONDRL**: `d MMMM yyyy` in Romanian (e.g., "4 iunie 2025")
- **eGrant**: `MMMM d yyyy` in Romanian (e.g., "iunie 4 2025")
- **MIDR**: Custom Romanian month parser (e.g., "IUNIE 4, 2025")

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) runs:

- **Schedule**: Every 3 hours via cron
- **Triggers**: Push/PR to main/master branches
- **Steps**:
  1. Checkout code
  2. Setup Java 21
  3. Set environment variables from secrets
  4. Build and compile
  5. Install Playwright dependencies
  6. Package application

**Required GitHub Secrets:**
- `DB_URL`: PostgreSQL connection string

## Development

### Adding a New Scraper

1. Create a new class in `com.parser.scrapers` package:

```java
package com.parser.scrapers;

import com.microsoft.playwright.Page;
import com.parser.ArticleScraper;
import com.parser.botconfig.TelegramBotConfig;

public class MyNewScraper implements ArticleScraper {
    private static final String URL = "https://example.com/news";

    @Override
    public String getUrl() {
        return URL;
    }

    @Override
    public void checkLatestArticles(TelegramBotConfig botConfig, Page page) {
        // Implement scraping logic
    }
}
```

2. Register in `Main.java`:

```java
List<ArticleScraper> scrapers = List.of(
    new OndrlScraper(),
    new EgrantScraper(),
    new MidrScraper(),
    new OnipmScraper(),
    new MyNewScraper()  // Add here
);
```

### Testing a Single Scraper

Temporarily modify `Main.java` to run only one scraper:

```java
List<ArticleScraper> scrapers = List.of(
    new MyNewScraper()  // Test only this one
);
```

### Debugging

- **Enable headed mode**: Change `setHeadless(true)` to `setHeadless(false)` in `Main.setupBrowserContext()`
- **Increase wait time**: Adjust `page.waitForTimeout(3000)` value
- **Check locators**: Print `locator.count()` before extracting text
- **Database issues**: Run `DBHelper.main()` to test connection

## Project Structure

```
Web-Scraping/
├── .env                    # Environment variables (DB credentials)
├── .github/
│   └── workflows/
│       └── ci.yml         # GitHub Actions workflow
├── pom.xml                # Maven configuration
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── parser/
│                   ├── Main.java              # Application entry point
│                   ├── ArticleScraper.java    # Scraper interface
│                   ├── botconfig/
│                   │   ├── TelegramBotConfig.java  # Bot configuration
│                   │   └── ChatConfig.java         # Chat IDs
│                   ├── db/
│                   │   └── DBHelper.java      # Database operations
│                   └── scrapers/
│                       ├── OnipmScraper.java
│                       ├── OndrlScraper.java
│                       ├── EgrantScraper.java
│                       └── MidrScraper.java
└── README.md
```

## Troubleshooting

### Bot Registration Fails
- Verify `BOT_TOKEN` is correct in `TelegramBotConfig.java`
- Check internet connection
- Ensure bot is active (talk to [@BotFather](https://t.me/botfather))

### Database Connection Errors
- Verify `.env` file exists and is properly formatted
- Test PostgreSQL connection independently
- Check firewall settings for database port
- Ensure `sslmode=require` matches your PostgreSQL configuration

### No Articles Found
- Verify target website is accessible
- Check if website structure has changed (update locators)
- Ensure dates match today's date
- Run in headed mode to visually inspect page

### Playwright Installation Issues
```bash
# Reinstall Playwright browsers
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

## License

This project is for educational and monitoring purposes.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Support

For issues and questions, please open an issue on GitHub.
