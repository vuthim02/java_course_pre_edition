package com.k8sdemo.controller;

import com.k8sdemo.service.GracefulShutdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final GracefulShutdownService shutdownService;
    private final DataSource dataSource;

    @GetMapping("/liveness")
    public ResponseEntity<Map<String, String>> liveness() {
        return ResponseEntity.ok(Map.of("status", "alive"));
    }

    @GetMapping("/readiness")
    public ResponseEntity<Map<String, String>> readiness() {
        if (shutdownService.isShuttingDown()) {
            return ResponseEntity.status(503)
                .body(Map.of("status", "not ready", "reason", "shutting down"));
        }
        try (var conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                return ResponseEntity.ok(Map.of("status", "ready"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(503)
                .body(Map.of("status", "not ready", "reason", "database unavailable"));
        }
        return ResponseEntity.status(503)
            .body(Map.of("status", "not ready", "reason", "unknown"));
    }

    @GetMapping("/startup")
    public ResponseEntity<Map<String, String>> startup() {
        return ResponseEntity.ok(Map.of("status", "started"));
    }
}
