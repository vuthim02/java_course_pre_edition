package com.platform.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    @NotBlank String name,
    @Email @NotBlank String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
