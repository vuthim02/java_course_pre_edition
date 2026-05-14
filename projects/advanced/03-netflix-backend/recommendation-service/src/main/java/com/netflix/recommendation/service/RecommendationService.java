package com.netflix.recommendation.service;

import com.netflix.recommendation.dto.RecommendationDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class RecommendationService {
    private final Random random = new Random();

    private static final List<RecommendationDTO> FALLBACK_RECOMMENDATIONS = List.of(
        new RecommendationDTO(1L, "Popular Movie", "Action", 0.95, "Trending now"),
        new RecommendationDTO(2L, "Top Series", "Drama", 0.92, "Highly rated"),
        new RecommendationDTO(3L, "New Release", "Comedy", 0.88, "Just added"),
        new RecommendationDTO(4L, "Classic Film", "Thriller", 0.85, "Critically acclaimed"),
        new RecommendationDTO(5L, "Award Winner", "Documentary", 0.82, "Award-winning content")
    );

    public List<RecommendationDTO> getRecommendations(Long userId) {
        return FALLBACK_RECOMMENDATIONS.stream()
                .map(r -> new RecommendationDTO(
                    r.contentId(), r.title(), r.genre(),
                    Math.min(1.0, r.score() + (random.nextDouble() - 0.5) * 0.1),
                    r.reason()))
                .toList();
    }

    public List<RecommendationDTO> getSimilarContent(Long contentId) {
        return FALLBACK_RECOMMENDATIONS.stream()
                .filter(r -> !r.contentId().equals(contentId))
                .limit(3)
                .toList();
    }
}
