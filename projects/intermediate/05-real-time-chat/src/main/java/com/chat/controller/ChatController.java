package com.chat.controller;

import com.chat.dto.ChatMessageRequest;
import com.chat.dto.ChatMessageResponse;
import com.chat.model.ChatMessage;
import com.chat.model.ChatRoom;
import com.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request) {
        ChatMessageResponse response = chatService.saveAndPublish(request);
        messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), response);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload ChatMessageRequest request) {
        messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId() + "/typing",
                request.getSender() + " is typing...");
    }

    @MessageMapping("/chat.join")
    public void joinRoom(@Payload ChatMessageRequest request) {
        ChatMessage joinMessage = ChatMessage.builder()
                .roomId(request.getRoomId())
                .sender(request.getSender())
                .content(request.getSender() + " joined the room")
                .type("JOIN")
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessageResponse response = chatService.saveMessage(joinMessage);
        messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), response);
    }

    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload ChatMessageRequest request) {
        ChatMessage leaveMessage = ChatMessage.builder()
                .roomId(request.getRoomId())
                .sender(request.getSender())
                .content(request.getSender() + " left the room")
                .type("LEAVE")
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessageResponse response = chatService.saveMessage(leaveMessage);
        messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), response);
    }

    @GetMapping("/api/rooms")
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        return ResponseEntity.ok(chatService.getAllRooms());
    }

    @PostMapping("/api/rooms")
    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomId, @RequestParam String name) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createRoom(roomId, name));
    }

    @GetMapping("/api/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getMessages(roomId, page, size));
    }

    @GetMapping("/api/rooms/{roomId}/history")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getChatHistory(roomId));
    }
}
