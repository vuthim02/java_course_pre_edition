package com.aibackend.dto;

import jakarta.validation.constraints.NotBlank;

public record SummarizeRequest(
    @NotBlank String text,
    Integer maxLength,
    String style
) {}
