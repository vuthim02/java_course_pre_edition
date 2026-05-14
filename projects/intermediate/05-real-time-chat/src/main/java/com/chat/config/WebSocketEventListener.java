package com.chat.config;

import com.chat.dto.ChatMessageRequest;
import com.chat.model.ChatMessage;
import com.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            log.info("User connected: {}", principal.getName());
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            log.info("User disconnected: {}", principal.getName());
        }
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        String destination = new String((byte[]) event.getMessage().getHeaders().get("simpDestination"));
        log.info("Subscription to: {}", destination);
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        log.info("Unsubscription event received");
    }
}
