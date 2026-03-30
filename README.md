# Live News Aggregator

Real-time news aggregator built with Spring Boot. Fetches articles from BBC and Google News RSS feeds, auto-categorizes them into 11 categories, and pushes live updates to the browser via WebSocket.

## Tech Stack

- **Java 21** + **Maven**
- **Spring Boot 3.4.1** (Web, JPA, WebSocket, Thymeleaf)
- **PostgreSQL** — article storage with duplicate detection (SHA-256 title hash)
- **Rome 2.1.0** — RSS feed parsing
- **STOMP over WebSocket** — real-time push to browser
- **SockJS** — WebSocket fallback for older browsers
- **Lombok** — boilerplate reduction

## Prerequisites

1. **Java 21** — [Download](https://adoptium.net/)
2. **Maven** — [Download](https://maven.apache.org/download.cgi)
3. **PostgreSQL** — [Download](https://www.postgresql.org/download/)

## Setup

### 1. Create the PostgreSQL database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE news_aggregator;

# Exit
\q
```

### 2. Configure database credentials

Open `src/main/resources/application.yml` and update:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/news_aggregator
    username: root
    password: root
```

### 3. Build and run

```bash
# From the project root directory
mvn clean install
mvn spring-boot:run
```

### 4. Open in browser

Go to: **http://localhost:8580**

## How It Works

1. The app polls 4 RSS feeds (BBC World + 3 Google News feeds) every 2 minutes
2. New articles are saved to PostgreSQL with duplicate detection (SHA-256 title hash)
3. Articles are auto-categorized into one of 11 categories based on title keywords
4. New articles are instantly pushed to the browser via WebSocket (`/topic/news`)
5. The page shows the 15 most recent articles — newest on top
6. If WebSocket disconnects, it reconnects with exponential backoff (up to 10 attempts), then falls back to REST API polling every 30 seconds

## Categories

Articles are automatically categorized by keyword matching (first match wins):

| Category | Color | Example Keywords |
|----------|-------|------------------|
| Gold | #FFD700 | gold, bullion, precious metal |
| Oil | #FF8C00 | crude, petroleum, opec, brent |
| Crypto | #00BFFF | bitcoin, ethereum, blockchain |
| Wars | #FF4444 | military, missile, airstrike, conflict |
| Defense | #FF6B6B | nato, pentagon, arms deal, weapons |
| Sanctions | #FF9800 | sanction, embargo, tariff |
| Elections | #9C27B0 | election, vote, ballot, campaign |
| Diplomacy | #4CAF50 | summit, treaty, united nations, g7 |
| Economy | #2196F3 | gdp, inflation, stock market, wall street |
| Politics | #E91E63 | president, congress, parliament, senate |
| World | #AAAAAA | international, global (default fallback) |

## RSS Feeds

| Name | Source |
|------|--------|
| BBC World | `http://feeds.bbci.co.uk/news/world/rss.xml` |
| Google News - Business | `https://news.google.com/rss/topics/...` |
| Google News - Politics | `https://news.google.com/rss/topics/...` |
| Google News - Top Stories | `https://news.google.com/rss` |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Main page (Thymeleaf SSR) |
| GET | `/api/v1` | REST API — latest articles as JSON |
| WS | `/ws-news` | WebSocket endpoint (STOMP + SockJS) |

## Project Structure

```
src/main/java/com/newsaggregator/
├── NewsAggregatorApplication.java     # Main app + @EnableScheduling
├── config/
│   ├── RssFeedProperties.java        # RSS feed configuration (YAML binding)
│   └── WebSocketConfig.java          # WebSocket/STOMP config
├── controller/
│   ├── NewsController.java           # Thymeleaf page controller (GET /)
│   └── NewsApiController.java        # REST API (GET /api/v1)
├── model/
│   ├── NewsArticle.java              # JPA Entity (news_articles table)
│   └── NewsArticleDto.java           # DTO for frontend
├── repository/
│   └── NewsArticleRepository.java    # JPA Repository
└── service/
    ├── CategoryDetector.java         # Keyword-based categorization (11 categories)
    ├── NewsService.java              # Business logic + scheduled polling
    └── RssFeedFetcher.java           # RSS feed fetching + parsing

src/main/resources/
├── application.yml                    # Configuration
└── templates/
    └── index.html                     # Frontend (dark theme, embedded CSS/JS)
```

## Customization

### Change poll interval

In `application.yml`:
```yaml
rss:
  poll:
    interval: 60000   # 1 minute (in milliseconds)
```

### Change max articles on page

```yaml
rss:
  max:
    articles: 12   # Show 12 instead of 15
```

### Add more RSS feeds

```yaml
rss:
  feeds:
    - name: BBC World
      url: http://feeds.bbci.co.uk/news/world/rss.xml
    # Add new feed:
    - name: Reuters Top News
      url: https://feeds.reuters.com/reuters/topNews
```

### Add custom categories

Edit `CategoryDetector.java` and add new entries to the `CATEGORY_KEYWORDS` map.

## Troubleshooting

- **"Connection refused" for PostgreSQL** — Make sure PostgreSQL is running: `pg_isready`
- **No articles showing** — Wait 2 minutes for the first RSS poll, or check logs for errors
- **WebSocket shows "Disconnected"** — The app reconnects automatically with exponential backoff, then falls back to polling. Check if port 8580 is available.
- **Database recreated on restart** — DDL auto is set to `create-drop` (development mode). Change to `update` or `validate` for persistence.
