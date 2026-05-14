package com.netflix.user.controller;

import com.netflix.user.dto.WatchHistoryDTO;
import com.netflix.user.service.WatchHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class WatchHistoryController {
    private final WatchHistoryService service;

    public WatchHistoryController(WatchHistoryService service) {
        this.service = service;
    }

    @GetMapping("/profile/{profileId}")
    public List<WatchHistoryDTO> getByProfile(@PathVariable Long profileId) {
        return service.findByProfileId(profileId);
    }

    @GetMapping("/profile/{profileId}/completed")
    public List<WatchHistoryDTO> getCompleted(@PathVariable Long profileId) {
        return service.getCompleted(profileId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WatchHistoryDTO record(@RequestBody WatchHistoryDTO dto) {
        return service.record(dto);
    }
}
