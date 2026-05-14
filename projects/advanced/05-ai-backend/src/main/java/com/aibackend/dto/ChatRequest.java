package com.aibackend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank String message,
    String systemPrompt,
    Double temperature
) {}
