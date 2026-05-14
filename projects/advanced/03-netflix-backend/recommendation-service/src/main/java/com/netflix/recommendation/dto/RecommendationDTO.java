package com.netflix.recommendation.dto;

public record RecommendationDTO(
    Long contentId,
    String title,
    String genre,
    Double score,
    String reason
) {}
