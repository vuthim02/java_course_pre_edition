package com.netflix.recommendation.controller;

import com.netflix.recommendation.dto.RecommendationDTO;
import com.netflix.recommendation.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/user/{userId}")
    public List<RecommendationDTO> getUserRecommendations(@PathVariable Long userId) {
        return service.getRecommendations(userId);
    }

    @GetMapping("/similar/{contentId}")
    public List<RecommendationDTO> getSimilarContent(@PathVariable Long contentId) {
        return service.getSimilarContent(contentId);
    }
}
