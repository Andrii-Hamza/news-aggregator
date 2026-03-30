package com.newsaggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "rss")
@Getter
@Setter
public class RssFeedProperties {

    private Poll poll = new Poll();
    private Max max = new Max();
    private List<FeedConfig> feeds = new ArrayList<>();

    @Getter
    @Setter
    public static class Poll {
        private long interval = 120000; // 2 minutes default
    }

    @Getter
    @Setter
    public static class Max {
        private int articles = 15;
    }

    @Getter
    @Setter
    public static class FeedConfig {
        private String name;
        private String url;
    }
}
