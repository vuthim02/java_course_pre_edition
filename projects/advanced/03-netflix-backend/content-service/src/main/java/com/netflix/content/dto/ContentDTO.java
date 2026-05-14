package com.netflix.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ContentDTO(
    Long id,
    @NotBlank String title,
    String description,
    @NotBlank String genre,
    @NotBlank String contentType,
    Integer releaseYear,
    @PositiveOrZero Double rating
) {}
