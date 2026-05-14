package com.saas.controller;

import com.saas.dto.UsageRecordDTO;
import com.saas.service.UsageTrackingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/usage")
public class UsageController {
    private final UsageTrackingService service;

    public UsageController(UsageTrackingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsageRecordDTO recordUsage(@RequestParam String tenantId,
                                       @RequestParam String metric,
                                       @RequestParam long value) {
        return service.recordUsage(tenantId, metric, value);
    }

    @GetMapping
    public List<UsageRecordDTO> getUsage(
            @RequestParam String tenantId,
            @RequestParam String metric,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return service.getUsage(tenantId, metric, start, end);
    }

    @GetMapping("/total")
    public long getTotalUsage(
            @RequestParam String tenantId,
            @RequestParam String metric,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return service.getTotalUsage(tenantId, metric, start, end);
    }
}
