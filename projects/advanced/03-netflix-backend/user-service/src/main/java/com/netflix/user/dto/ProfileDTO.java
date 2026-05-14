package com.netflix.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileDTO(
    Long id,
    @NotBlank String name,
    String avatarUrl,
    boolean kidProfile
) {}
