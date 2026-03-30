package com.newsaggregator.repository;

import com.newsaggregator.model.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    // Get latest articles ordered by published date (newest first)
    List<NewsArticle> findAllByOrderByPublishedAtDesc();

    // Check if article already exists by title hash (avoid duplicates)
    boolean existsByTitleHash(String titleHash);

    // Count all articles
    long count();
}
