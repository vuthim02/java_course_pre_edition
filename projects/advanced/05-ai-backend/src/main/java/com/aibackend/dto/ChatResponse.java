package com.aibackend.dto;

public record ChatResponse(
    String reply,
    long tokensUsed,
    String model
) {}
