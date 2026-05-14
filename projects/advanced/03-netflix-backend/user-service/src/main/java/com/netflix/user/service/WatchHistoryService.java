package com.netflix.user.service;

import com.netflix.user.dto.WatchHistoryDTO;
import com.netflix.user.model.WatchHistory;
import com.netflix.user.repository.WatchHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchHistoryService {
    private final WatchHistoryRepository repository;

    public WatchHistoryService(WatchHistoryRepository repository) {
        this.repository = repository;
    }

    public List<WatchHistoryDTO> findByProfileId(Long profileId) {
        return repository.findByProfileId(profileId).stream().map(this::toDto).toList();
    }

    public WatchHistoryDTO record(WatchHistoryDTO dto) {
        var record = new WatchHistory();
        record.setProfileId(dto.profileId());
        record.setContentId(dto.contentId());
        record.setProgressSeconds(dto.progressSeconds());
        record.setCompleted(dto.completed());
        return toDto(repository.save(record));
    }

    public List<WatchHistoryDTO> getCompleted(Long profileId) {
        return repository.findByProfileIdAndCompleted(profileId, true)
                .stream().map(this::toDto).toList();
    }

    private WatchHistoryDTO toDto(WatchHistory record) {
        return new WatchHistoryDTO(record.getId(), record.getProfileId(), record.getContentId(),
                record.getProgressSeconds(), record.isCompleted(), record.getWatchedAt());
    }
}
