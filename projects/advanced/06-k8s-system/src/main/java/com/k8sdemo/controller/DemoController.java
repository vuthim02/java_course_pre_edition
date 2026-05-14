package com.k8sdemo.controller;

import com.k8sdemo.service.LeaderElectionService;
import com.k8sdemo.service.GracefulShutdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {

    private final LeaderElectionService leaderElectionService;
    private final GracefulShutdownService shutdownService;

    @Value("${spring.application.name:k8s-system}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        var podName = System.getenv("HOSTNAME");
        if (podName == null || podName.isBlank()) {
            podName = "unknown";
        }
        var ip = "unknown";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            // ignore
        }
        return ResponseEntity.ok(Map.of(
            "appName", appName,
            "version", appVersion,
            "podName", podName,
            "ip", ip
        ));
    }

    @GetMapping("/config/{key}")
    public ResponseEntity<Map<String, String>> config(@PathVariable String key) {
        var value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key, "not found");
        }
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @GetMapping("/leader")
    public ResponseEntity<?> leader() {
        var current = leaderElectionService.getCurrentLeader();
        if (current.isPresent()) {
            var lease = current.get();
            return ResponseEntity.ok(Map.of(
                "leader", lease.getInstanceId(),
                "acquiredAt", lease.getAcquiredAt().toString(),
                "expiresAt", lease.getExpiresAt().toString()
            ));
        }
        return ResponseEntity.ok(Map.of("leader", "none"));
    }

    @PostMapping("/shutdown")
    public ResponseEntity<Map<String, String>> shutdown() {
        var hostname = System.getenv("HOSTNAME");
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            shutdownService.initiateShutdown();
        }, "api-shutdown-trigger").start();
        return ResponseEntity.ok(Map.of(
            "status", "shutdown initiated",
            "instance", hostname != null ? hostname : "unknown"
        ));
    }
}
