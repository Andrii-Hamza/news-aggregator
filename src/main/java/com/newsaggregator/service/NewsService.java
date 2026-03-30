package com.newsaggregator.service;

import com.newsaggregator.config.RssFeedProperties;
import com.newsaggregator.model.NewsArticle;
import com.newsaggregator.model.NewsArticleDto;
import com.newsaggregator.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsArticleRepository repository;
    private final RssFeedFetcher feedFetcher;
    private final RssFeedProperties feedProperties;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Get the latest articles (up to max configured).
     */
    public List<NewsArticleDto> getLatestArticles() {
        List<NewsArticle> articles = repository.findAllByOrderByPublishedAtDesc();

        return articles.stream()
                .limit(feedProperties.getMax().getArticles())
                .map(NewsArticleDto::fromEntity)
                .toList();
    }

    /**
     * Scheduled task: fetch all RSS feeds and save new articles.
     * Runs at the interval configured in application.yml.
     * Also runs once at startup (initialDelay = 0).
     */
    @Scheduled(fixedDelayString = "${rss.poll.interval}", initialDelay = 0)
    @Transactional
    public void fetchAndUpdateNews() {
        log.info("=== Starting RSS feed poll ===");

        List<NewsArticle> newArticles = new ArrayList<>();

        for (RssFeedProperties.FeedConfig feed : feedProperties.getFeeds()) {
            List<NewsArticle> fetched = feedFetcher.fetchFeed(feed);

            for (NewsArticle article : fetched) {
                // Skip if we already have this article
                if (!repository.existsByTitleHash(article.getTitleHash())) {
                    NewsArticle saved = repository.save(article);
                    newArticles.add(saved);
                }
            }
        }

        // Clean up old articles — keep only the latest N
        cleanupOldArticles();

        // If there are new articles, broadcast them via WebSocket
        if (!newArticles.isEmpty()) {
            log.info("Broadcasting {} new articles via WebSocket", newArticles.size());
            List<NewsArticleDto> latestArticles = getLatestArticles();
            messagingTemplate.convertAndSend("/topic/news", latestArticles);
        }

        log.info("=== RSS feed poll complete. {} new articles found ===", newArticles.size());
    }

    /**
     * Remove oldest articles if we exceed the max count.
     */
    private void cleanupOldArticles() {
        long count = repository.count();
        int maxArticles = feedProperties.getMax().getArticles();

        if (count > maxArticles) {
            // Get all articles sorted by date, skip the ones we want to keep
            List<NewsArticle> all = repository.findAllByOrderByPublishedAtDesc();
            List<NewsArticle> toDelete = all.stream()
                    .skip(maxArticles)
                    .toList();

            repository.deleteAll(toDelete);
            log.info("Cleaned up {} old articles", toDelete.size());
        }
    }
}
