package com.aibackend.dto;

import java.util.Map;

public record ModerationResult(
    boolean flagged,
    Map<String, Double> scores,
    String message
) {}
