package com.netflix.user.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watch_history")
public class WatchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "progress_seconds")
    private Integer progressSeconds;

    @Column(name = "completed")
    private boolean completed;

    @Column(name = "watched_at", nullable = false, updatable = false)
    private LocalDateTime watchedAt;

    @PrePersist
    protected void onCreate() {
        watchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Integer getProgressSeconds() { return progressSeconds; }
    public void setProgressSeconds(Integer progressSeconds) { this.progressSeconds = progressSeconds; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getWatchedAt() { return watchedAt; }
}
