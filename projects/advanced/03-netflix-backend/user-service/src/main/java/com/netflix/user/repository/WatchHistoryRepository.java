package com.netflix.user.repository;

import com.netflix.user.model.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    List<WatchHistory> findByProfileId(Long profileId);
    List<WatchHistory> findByProfileIdAndCompleted(Long profileId, boolean completed);
}
