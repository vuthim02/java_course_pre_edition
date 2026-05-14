package com.netflix.user.dto;

import java.time.LocalDateTime;

public record WatchHistoryDTO(
    Long id,
    Long profileId,
    Long contentId,
    Integer progressSeconds,
    boolean completed,
    LocalDateTime watchedAt
) {}
