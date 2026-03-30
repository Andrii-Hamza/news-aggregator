package com.newsaggregator.controller;

import com.newsaggregator.model.NewsArticleDto;
import com.newsaggregator.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/")
    public String index(Model model) {
        List<NewsArticleDto> articles = newsService.getLatestArticles();
        model.addAttribute("articles", articles);
        return "index";
    }
}
