package com.newsaggregator.service;

import com.newsaggregator.config.RssFeedProperties;
import com.newsaggregator.model.NewsArticle;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RssFeedFetcher {

    private final CategoryDetector categoryDetector;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    /**
     * Fetch articles from a single RSS feed URL.
     */
    public List<NewsArticle> fetchFeed(RssFeedProperties.FeedConfig feedConfig) {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(feedConfig.getUrl()))
                    .header("User-Agent", "NewsAggregator/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            SyndFeedInput input = new SyndFeedInput();
            input.setAllowDoctypes(true);
            SyndFeed feed = input.build(
                    new XmlReader(new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)))
            );

            for (SyndEntry entry : feed.getEntries()) {
                try {
                    String rawTitle = entry.getTitle();
                    String title;
                    String source;

                    if (feedConfig.isGoogleNews()) {
                        title = cleanGoogleNewsTitle(rawTitle);
                        source = extractGoogleNewsSource(rawTitle, feedConfig.getName());
                    } else {
                        title = rawTitle != null ? rawTitle.trim() : "Untitled";
                        source = feedConfig.getName();
                    }

                    String description = extractDescription(entry);
                    String link = entry.getLink();
                    LocalDateTime publishedAt = convertDate(entry.getPublishedDate());
                    String category = categoryDetector.detectCategory(title, description);
                    String titleHash = hashString(normalizeForHash(title));

                    NewsArticle article = NewsArticle.builder()
                            .title(title)
                            .link(link)
                            .description(description)
                            .category(category)
                            .source(source)
                            .publishedAt(publishedAt)
                            .titleHash(titleHash)
                            .build();

                    articles.add(article);
                } catch (Exception e) {
                    log.warn("Failed to parse entry: {}", entry.getTitle(), e);
                }
            }

            log.info("Fetched {} articles from {}", articles.size(), feedConfig.getName());

        } catch (Exception e) {
            log.error("Failed to fetch feed: {}", feedConfig.getName(), e);
        }

        return articles;
    }

    /**
     * Google News titles have format: "Article Title - Source Name".
     * Extracts the clean title without the source suffix.
     */
    private String cleanGoogleNewsTitle(String rawTitle) {
        if (rawTitle == null) return "Untitled";
        int lastDash = rawTitle.lastIndexOf(" - ");
        if (lastDash > 0) {
            return rawTitle.substring(0, lastDash).trim();
        }
        return rawTitle.trim();
    }

    /**
     * Extract source name from Google News title format.
     */
    private String extractGoogleNewsSource(String rawTitle, String defaultSource) {
        if (rawTitle == null) return defaultSource;
        int lastDash = rawTitle.lastIndexOf(" - ");
        if (lastDash > 0 && lastDash < rawTitle.length() - 3) {
            return rawTitle.substring(lastDash + 3).trim();
        }
        return defaultSource;
    }

    /**
     * Extract description/summary from an RSS entry.
     */
    private String extractDescription(SyndEntry entry) {
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            String desc = entry.getDescription().getValue()
                    .replaceAll("<[^>]+>", "")  // strip HTML tags
                    .trim();
            if (desc.length() > 2000) {
                desc = desc.substring(0, 2000);
            }
            return desc.isEmpty() ? null : desc;
        }
        return null;
    }

    /**
     * Normalize title for hashing to improve cross-source deduplication.
     * Lowercases, strips punctuation, and removes common prefixes like "Breaking:".
     */
    private String normalizeForHash(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("^(breaking|urgent|update|just in|developing):\\s*", "")
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Convert java.util.Date to LocalDateTime.
     */
    private LocalDateTime convertDate(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Create a SHA-256 hash of the title to detect duplicates.
     */
    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
