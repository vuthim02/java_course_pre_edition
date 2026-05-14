package com.netflix.gateway.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/content")
    public Map<String, String> contentFallback() {
        return Map.of("status", "DOWN", "message", "Content service is temporarily unavailable");
    }

    @GetMapping("/recommendations")
    public Map<String, String> recommendationsFallback() {
        return Map.of("status", "DOWN", "message", "Recommendation service is temporarily unavailable");
    }

    @GetMapping("/user")
    public Map<String, String> userFallback() {
        return Map.of("status", "DOWN", "message", "User service is temporarily unavailable");
    }
}
