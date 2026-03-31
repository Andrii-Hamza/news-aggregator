package com.newsaggregator.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticleDto {

    private Long id;
    private String title;
    private String link;
    private String description;
    private String category;
    private String source;
    private LocalDateTime publishedAt;

    public static NewsArticleDto fromEntity(NewsArticle article) {
        return NewsArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .link(article.getLink())
                .description(article.getDescription())
                .category(article.getCategory())
                .source(article.getSource())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}
