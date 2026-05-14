package com.chat.service;

import com.chat.dto.ChatMessageRequest;
import com.chat.dto.ChatMessageResponse;
import com.chat.model.ChatMessage;
import com.chat.model.ChatRoom;
import com.chat.repository.ChatMessageRepository;
import com.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public ChatMessageResponse saveAndPublish(ChatMessageRequest request) {
        ChatRoom room = roomRepository.findByRoomId(request.getRoomId())
                .orElseGet(() -> roomRepository.save(ChatRoom.builder()
                        .roomId(request.getRoomId())
                        .name(request.getRoomId())
                        .build()));

        ChatMessage message = ChatMessage.builder()
                .roomId(room.getRoomId())
                .sender(request.getSender())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : "CHAT")
                .timestamp(LocalDateTime.now())
                .build();
        message = messageRepository.save(message);

        ChatMessageResponse response = toResponse(message);
        redisTemplate.convertAndSend("chat:" + request.getRoomId(), response);
        return response;
    }

    public ChatMessageResponse saveMessage(ChatMessage message) {
        String roomId = message.getRoomId();
        ChatRoom room = roomRepository.findByRoomId(roomId)
                .orElseGet(() -> roomRepository.save(ChatRoom.builder()
                        .roomId(roomId)
                        .name(roomId)
                        .build()));
        ChatMessage saved = messageRepository.save(message);
        return toResponse(saved);
    }

    public Page<ChatMessageResponse> getMessages(String roomId, int page, int size) {
        return messageRepository.findByRoomIdOrderByTimestampDesc(roomId,
                        PageRequest.of(page, size, Sort.by("timestamp").descending()))
                .map(this::toResponse);
    }

    public List<ChatMessage> getChatHistory(String roomId) {
        return messageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }

    public ChatRoom createRoom(String roomId, String name) {
        if (roomRepository.existsByRoomId(roomId)) {
            throw new RuntimeException("Room already exists");
        }
        return roomRepository.save(ChatRoom.builder().roomId(roomId).name(name).build());
    }

    public List<ChatRoom> getAllRooms() {
        return roomRepository.findAll();
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .sender(message.getSender())
                .content(message.getContent())
                .type(message.getType())
                .timestamp(message.getTimestamp())
                .build();
    }
}
