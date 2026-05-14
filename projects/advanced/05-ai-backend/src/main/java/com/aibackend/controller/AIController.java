package com.aibackend.controller;

import com.aibackend.dto.ChatRequest;
import com.aibackend.dto.ChatResponse;
import com.aibackend.dto.ModerationResult;
import com.aibackend.dto.SummarizeRequest;
import com.aibackend.service.AIService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat/completions")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return aiService.chat(request);
    }

    @PostMapping("/summarization")
    public ChatResponse summarize(@Valid @RequestBody SummarizeRequest request) {
        return aiService.summarize(request);
    }

    @PostMapping("/moderation")
    public ModerationResult moderate(@RequestBody Map<String, String> body) {
        return aiService.moderate(body.get("text"));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request) {
        var emitter = new SseEmitter(300_000L);
        try {
            var response = aiService.streamChat(request);
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(response));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
