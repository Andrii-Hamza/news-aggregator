package com.newsaggregator.controller;

import com.newsaggregator.model.NewsArticleDto;
import com.newsaggregator.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NewsApiController {

    private final NewsService newsService;

    /**
     * REST endpoint to get latest articles.
     * Used as fallback if WebSocket connection fails.
     */
    @GetMapping
    public ResponseEntity<List<NewsArticleDto>> getLatestNews() {
        return ResponseEntity.ok(newsService.getLatestArticles());
    }
}
