package com.aibackend.service;

import com.aibackend.dto.ChatRequest;
import com.aibackend.dto.ChatResponse;
import com.aibackend.dto.ModerationResult;
import com.aibackend.dto.SummarizeRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIService {
    private final ChatModel chatModel;

    @Value("${app.moderation.enabled:true}")
    private boolean moderationEnabled;

    public AIService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public ChatResponse chat(ChatRequest request) {
        var promptTemplate = new PromptTemplate(request.systemPrompt() != null
                ? request.systemPrompt() + "\n{message}"
                : "{message}");
        var prompt = promptTemplate.create(Map.of("message", request.message()));
        var response = chatModel.call(prompt);
        var result = response.getResult();
        var content = result.getOutput().getContent();
        var metadata = result.getMetadata();
        return new ChatResponse(
            content,
            metadata != null ? 0 : 0,
            metadata != null ? "ai-model" : "ai-model"
        );
    }

    public ChatResponse summarize(SummarizeRequest request) {
        var promptTemplate = new PromptTemplate(
            "Summarize the following text in a {style} style" +
            (request.maxLength() != null ? " within " + request.maxLength() + " characters." : ".") +
            "\n\nText: {text}"
        );
        var prompt = promptTemplate.create(Map.of(
            "text", request.text(),
            "style", request.style() != null ? request.style() : "concise"
        ));
        var response = chatModel.call(prompt);
        var result = response.getResult();
        var content = result.getOutput().getContent();
        var metadata = result.getMetadata();
        return new ChatResponse(
            content,
            metadata != null ? 0 : 0,
            metadata != null ? "ai-model" : "ai-model"
        );
    }

    public ModerationResult moderate(String text) {
        var promptTemplate = new PromptTemplate(
            "Analyze the following text for harmful content. " +
            "Rate each category (hate, sexual, violence, self-harm) from 0.0 to 1.0. " +
            "Respond with a JSON object.\n\nText: {text}"
        );
        var prompt = promptTemplate.create(Map.of("text", text));
        var response = chatModel.call(prompt);
        var content = response.getResult().getOutput().getContent();

        var flagged = content.toLowerCase().contains("flagged") ||
                     content.toLowerCase().contains("true");

        return new ModerationResult(
            flagged,
            Map.of("score", flagged ? 0.8 : 0.1),
            content
        );
    }

    public ChatResponse streamChat(ChatRequest request) {
        return chat(request);
    }
}
