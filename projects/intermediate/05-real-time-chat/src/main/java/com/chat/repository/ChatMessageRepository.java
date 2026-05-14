package com.chat.repository;

import com.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);

    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);
}
