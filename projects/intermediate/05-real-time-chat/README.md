# Project 5: Real-time Chat System

**Concepts:** Spring WebSocket, STOMP, Chat Rooms, Real-time Messaging, Message Persistence, User Presence, Typing Indicators

## Project Structure

```
src/main/java/com/example/chat/
├── ChatApplication.java
├── config/
│   ├── WebSocketConfig.java
│   ├── SecurityConfig.java
│   └── WebMvcConfig.java
├── controller/
│   ├── ChatRoomController.java
│   ├── MessageController.java
│   └── UserPresenceController.java
├── service/
│   ├── ChatRoomService.java
│   ├── MessageService.java
│   └── UserPresenceService.java
├── repository/
│   ├── ChatRoomRepository.java
│   ├── ChatMessageRepository.java
│   └── ChatUserRepository.java
├── model/
│   ├── ChatRoom.java
│   ├── ChatMessage.java
│   ├── MessageStatus.java
│   └── ChatUser.java
├── dto/
│   ├── ChatRoomRequest.java
│   ├── ChatRoomResponse.java
│   ├── ChatMessageRequest.java
│   ├── ChatMessageResponse.java
│   ├── TypingIndicator.java
│   └── PresenceEvent.java
├── websocket/
│   ├── ChatWebSocketHandler.java
│   └── WebSocketEventListener.java
└── exception/
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yml
└── static/
    └── index.html
```

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>realtime-chat</artifactId>
    <version>1.0.0</version>
    <name>realtime-chat</name>
    <description>Real-time Chat with WebSockets</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## application.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:chatdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    open-in-view: false

server:
  port: 8080

app:
  websocket:
    allowed-origins: "*"
    endpoint: /ws
    app-prefix: /app
    topic-prefix: /topic
    user-prefix: /user
    presence-topic: /topic/presence
    typing-topic: /topic/typing
```

## ChatApplication.java

```java
package com.example.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
```

## model/ChatUser.java

```java
package com.example.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_users")
public class ChatUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "is_online")
    private boolean isOnline = false;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
    }

    public ChatUser() {}

    public ChatUser(String username) {
        this.username = username;
        this.displayName = username;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

## model/ChatRoom.java

```java
package com.example.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "is_private")
    private boolean isPrivate = false;

    @Column(name = "max_participants")
    private int maxParticipants = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ChatRoom() {}

    public ChatRoom(String name, String createdBy) {
        this.name = name;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

## model/MessageStatus.java

```java
package com.example.chat.model;

public enum MessageStatus {
    SENT, DELIVERED, READ
}
```

## model/ChatMessage.java

```java
package com.example.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String sender;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "room_name")
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = MessageStatus.SENT;
    }

    public ChatMessage() {}

    public ChatMessage(String content, String sender, Long roomId, String roomName, String messageType) {
        this.content = content;
        this.sender = sender;
        this.roomId = roomId;
        this.roomName = roomName;
        this.messageType = messageType;
        this.status = MessageStatus.SENT;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

## repository/ChatUserRepository.java

```java
package com.example.chat.repository;

import com.example.chat.model.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    Optional<ChatUser> findByUsername(String username);
    boolean existsByUsername(String username);
    List<ChatUser> findByIsOnlineTrue();
    List<ChatUser> findByIsOnlineFalse();
    long countByIsOnlineTrue();
}
```

## repository/ChatRoomRepository.java

```java
package com.example.chat.repository;

import com.example.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    boolean existsByName(String name);
    List<ChatRoom> findByIsPrivateFalse();
}
```

## repository/ChatMessageRepository.java

```java
package com.example.chat.repository;

import com.example.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtAsc(Long roomId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.roomId = :roomId AND m.status = 'SENT'")
    long countUnreadByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<ChatMessage> findMessagesSince(@Param("roomId") Long roomId,
                                         @Param("since") java.time.LocalDateTime since);
}
```

## dto/ChatRoomRequest.java

```java
package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRoomRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @Size(max = 500) String description,
    boolean isPrivate,
    int maxParticipants
) {}
```

## dto/ChatRoomResponse.java

```java
package com.example.chat.dto;

import com.example.chat.model.ChatRoom;
import java.time.LocalDateTime;

public record ChatRoomResponse(
    Long id, String name, String description, String createdBy,
    boolean isPrivate, int maxParticipants,
    LocalDateTime createdAt
) {
    public static ChatRoomResponse fromEntity(ChatRoom room) {
        return new ChatRoomResponse(
            room.getId(), room.getName(), room.getDescription(),
            room.getCreatedBy(), room.isPrivate(),
            room.getMaxParticipants(), room.getCreatedAt()
        );
    }
}
```

## dto/ChatMessageRequest.java

```java
package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessageRequest(
    @NotBlank String content,
    @NotNull Long roomId,
    String messageType,
    String fileUrl,
    String fileName
) {}
```

## dto/ChatMessageResponse.java

```java
package com.example.chat.dto;

import com.example.chat.model.ChatMessage;
import com.example.chat.model.MessageStatus;
import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id, String content, String sender,
    Long roomId, String roomName,
    MessageStatus status, String messageType,
    String fileUrl, String fileName,
    LocalDateTime createdAt
) {
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(), message.getContent(), message.getSender(),
            message.getRoomId(), message.getRoomName(),
            message.getStatus(), message.getMessageType(),
            message.getFileUrl(), message.getFileName(),
            message.getCreatedAt()
        );
    }
}
```

## dto/TypingIndicator.java

```java
package com.example.chat.dto;

public record TypingIndicator(
    String username,
    Long roomId,
    boolean isTyping
) {}
```

## dto/PresenceEvent.java

```java
package com.example.chat.dto;

public record PresenceEvent(
    String username,
    String displayName,
    boolean online,
    String status
) {}
```

## repository (in-memory presence tracking)

```java
// Not a repository bean - handled in service
```

## service/ChatRoomService.java

```java
package com.example.chat.service;

import com.example.chat.dto.ChatRoomRequest;
import com.example.chat.dto.ChatRoomResponse;
import com.example.chat.model.ChatRoom;
import com.example.chat.repository.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    public ChatRoomResponse createRoom(ChatRoomRequest request, String createdBy) {
        if (chatRoomRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Room already exists: " + request.name());
        }
        ChatRoom room = new ChatRoom(request.name(), createdBy);
        room.setDescription(request.description());
        room.setPrivate(request.isPrivate());
        room.setMaxParticipants(request.maxParticipants() > 0 ? request.maxParticipants() : 100);
        ChatRoom saved = chatRoomRepository.save(room);
        return ChatRoomResponse.fromEntity(saved);
    }

    public ChatRoomResponse getRoom(Long id) {
        ChatRoom room = chatRoomRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + id));
        return ChatRoomResponse.fromEntity(room);
    }

    public ChatRoomResponse getRoomByName(String name) {
        List<ChatRoom> rooms = chatRoomRepository.findByIsPrivateFalse();
        return rooms.stream()
            .filter(r -> r.getName().equals(name))
            .findFirst()
            .map(ChatRoomResponse::fromEntity)
            .orElse(null);
    }

    public List<ChatRoomResponse> getPublicRooms() {
        return chatRoomRepository.findByIsPrivateFalse().stream()
            .map(ChatRoomResponse::fromEntity).toList();
    }

    public void deleteRoom(Long id) {
        if (!chatRoomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found: " + id);
        }
        chatRoomRepository.deleteById(id);
    }
}
```

## service/MessageService.java

```java
package com.example.chat.service;

import com.example.chat.dto.ChatMessageRequest;
import com.example.chat.dto.ChatMessageResponse;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.MessageStatus;
import com.example.chat.repository.ChatMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageService {

    private final ChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(ChatMessageRepository messageRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public ChatMessageResponse sendMessage(ChatMessageRequest request, String sender) {
        ChatMessage message = new ChatMessage(
            request.content(),
            sender,
            request.roomId(),
            null,
            request.messageType() != null ? request.messageType() : "CHAT"
        );
        message.setFileUrl(request.fileUrl());
        message.setFileName(request.fileName());

        ChatMessage saved = messageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(saved);

        messagingTemplate.convertAndSend(
            "/topic/room/" + request.roomId(),
            response
        );

        return response;
    }

    public ChatMessageResponse sendPrivateMessage(ChatMessageRequest request, String sender, String recipient) {
        ChatMessage message = new ChatMessage(
            request.content(), sender, request.roomId(),
            null, "PRIVATE"
        );
        ChatMessage saved = messageRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.fromEntity(saved);

        messagingTemplate.convertAndSendToUser(
            recipient, "/queue/messages", response
        );
        messagingTemplate.convertAndSendToUser(
            sender, "/queue/messages", response
        );

        return response;
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessageHistory(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
            .map(ChatMessageResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRecentMessages(Long roomId) {
        return messageRepository.findTop50ByRoomIdOrderByCreatedAtAsc(roomId)
            .stream().map(ChatMessageResponse::fromEntity).toList();
    }

    public void markAsRead(Long messageId) {
        ChatMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        message.setStatus(MessageStatus.READ);
        messageRepository.save(message);
    }
}
```

## service/UserPresenceService.java

```java
package com.example.chat.service;

import com.example.chat.dto.PresenceEvent;
import com.example.chat.model.ChatUser;
import com.example.chat.repository.ChatUserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserPresenceService {

    private final ChatUserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public UserPresenceService(ChatUserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public ChatUser registerUser(String username) {
        if (userRepository.existsByUsername(username)) {
            return userRepository.findByUsername(username).get();
        }
        ChatUser user = new ChatUser(username);
        return userRepository.save(user);
    }

    public void userConnected(String username, String sessionId) {
        ChatUser user = registerUser(username);
        user.setOnline(true);
        user.setSessionId(sessionId);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        broadcastPresence(user, true);
    }

    public void userDisconnected(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(false);
            user.setSessionId(null);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);

            broadcastPresence(user, false);
        });
    }

    public void userDisconnectedBySessionId(String sessionId) {
        userRepository.findAll().stream()
            .filter(u -> sessionId.equals(u.getSessionId()))
            .findFirst()
            .ifPresent(user -> userDisconnected(user.getUsername()));
    }

    private void broadcastPresence(ChatUser user, boolean online) {
        messagingTemplate.convertAndSend("/topic/presence", new PresenceEvent(
            user.getUsername(), user.getDisplayName(), online,
            online ? "ONLINE" : "OFFLINE"
        ));
    }

    public List<ChatUser> getOnlineUsers() {
        return userRepository.findByIsOnlineTrue();
    }

    public long getOnlineCount() {
        return userRepository.countByIsOnlineTrue();
    }

    public boolean isUserOnline(String username) {
        return userRepository.findByUsername(username)
            .map(ChatUser::isOnline).orElse(false);
    }
}
```

## config/WebSocketConfig.java

```java
package com.example.chat.config;

import com.example.chat.websocket.WebSocketEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketEventListener());
    }
}
```

## websocket/WebSocketEventListener.java

```java
package com.example.chat.websocket;

import com.example.chat.service.UserPresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class WebSocketEventListener implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    @Lazy
    private UserPresenceService presenceService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (StompCommand.CONNECT.equals(command)) {
                String username = accessor.getFirstNativeHeader("username");
                if (username != null) {
                    log.info("WebSocket connected: {}", username);
                    presenceService.userConnected(username, accessor.getSessionId());
                    accessor.getSessionAttributes().put("username", username);
                }
            } else if (StompCommand.DISCONNECT.equals(command)) {
                String username = (String) accessor.getSessionAttributes().get("username");
                if (username != null) {
                    log.info("WebSocket disconnected: {}", username);
                    presenceService.userDisconnected(username);
                } else {
                    presenceService.userDisconnectedBySessionId(accessor.getSessionId());
                }
            }
        }

        return message;
    }
}
```

## config/SecurityConfig.java

```java
package com.example.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/topic/**").permitAll()
                .requestMatchers("/app/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
```

## config/WebMvcConfig.java

```java
package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

## controller/ChatRoomController.java

```java
package com.example.chat.controller;

import com.example.chat.dto.ChatRoomRequest;
import com.example.chat.dto.ChatRoomResponse;
import com.example.chat.service.ChatRoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createRoom(
            @Valid @RequestBody ChatRoomRequest request,
            @RequestParam(defaultValue = "anonymous") String createdBy) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(chatRoomService.createRoom(request, createdBy));
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getPublicRooms() {
        return ResponseEntity.ok(chatRoomService.getPublicRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoomResponse> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(chatRoomService.getRoom(id));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ChatRoomResponse> getRoomByName(@PathVariable String name) {
        ChatRoomResponse room = chatRoomService.getRoomByName(name);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        chatRoomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
```

## controller/MessageController.java

```java
package com.example.chat.controller;

import com.example.chat.dto.ChatMessageRequest;
import com.example.chat.dto.ChatMessageResponse;
import com.example.chat.dto.TypingIndicator;
import com.example.chat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageService messageService,
                             SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageResponse sendMessage(@Payload ChatMessageRequest request,
                                           Principal principal) {
        String sender = principal != null ? principal.getName() : "anonymous";
        return messageService.sendMessage(request, sender);
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessageToRoom(@DestinationVariable Long roomId,
                                  @Payload ChatMessageRequest request,
                                  Principal principal) {
        String sender = principal != null ? principal.getName() : "anonymous";
        messageService.sendMessage(request, sender);
    }

    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload TypingIndicator indicator) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + indicator.roomId() + "/typing",
            indicator
        );
    }

    @MessageMapping("/chat.privateMessage")
    public void sendPrivateMessage(@Payload ChatMessageRequest request,
                                   @Payload String recipient,
                                   Principal principal) {
        String sender = principal != null ? principal.getName() : "anonymous";
        messageService.sendPrivateMessage(request, sender, recipient);
    }

    @RestController
    @RequestMapping("/api/messages")
    public static class MessageRestController {

        private final MessageService messageService;

        public MessageRestController(MessageService messageService) {
            this.messageService = messageService;
        }

        @PostMapping
        public ResponseEntity<ChatMessageResponse> sendMessage(
                @Valid @RequestBody ChatMessageRequest request,
                @RequestParam(defaultValue = "anonymous") String sender) {
            return ResponseEntity.ok(messageService.sendMessage(request, sender));
        }

        @GetMapping("/room/{roomId}")
        public ResponseEntity<Page<ChatMessageResponse>> getMessageHistory(
                @PathVariable Long roomId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "50") int size) {
            return ResponseEntity.ok(messageService.getMessageHistory(roomId, page, size));
        }

        @GetMapping("/room/{roomId}/recent")
        public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(
                @PathVariable Long roomId) {
            return ResponseEntity.ok(messageService.getRecentMessages(roomId));
        }

        @PostMapping("/{messageId}/read")
        public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
            messageService.markAsRead(messageId);
            return ResponseEntity.noContent().build();
        }
    }
}
```

## controller/UserPresenceController.java

```java
package com.example.chat.controller;

import com.example.chat.model.ChatUser;
import com.example.chat.service.UserPresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presence")
public class UserPresenceController {

    private final UserPresenceService presenceService;

    public UserPresenceController(UserPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @PostMapping("/register")
    public ResponseEntity<ChatUser> registerUser(@RequestParam String username) {
        return ResponseEntity.ok(presenceService.registerUser(username));
    }

    @GetMapping("/online")
    public ResponseEntity<List<ChatUser>> getOnlineUsers() {
        return ResponseEntity.ok(presenceService.getOnlineUsers());
    }

    @GetMapping("/online/count")
    public ResponseEntity<Map<String, Long>> getOnlineCount() {
        return ResponseEntity.ok(Map.of("count", presenceService.getOnlineCount()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Boolean>> isUserOnline(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("online", presenceService.isUserOnline(username)));
    }
}
```

## exception/GlobalExceptionHandler.java

```java
package com.example.chat.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not Found");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Bad Request");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Failed");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
```

## WebSocket Event Flow

```
Client → STOMP CONNECT (with username header)
  → WebSocketEventListener.preSend() captures session
  → UserPresenceService.userConnected() → broadcasts /topic/presence

Client → /app/chat.sendMessage/{roomId}
  → MessageController.sendMessageToRoom()
  → MessageService.sendMessage() → persists to DB
  → SimpMessagingTemplate.send() to /topic/room/{roomId}

Client → /app/chat.typing
  → TypingIndicator sent to /topic/room/{roomId}/typing

Client DISCONNECT
  → WebSocketEventListener DICONNECT
  → UserPresenceService.userDisconnected() → broadcasts /topic/presence
```

## WebSocket STOMP Endpoints

| Destination | Direction | Description |
|-------------|-----------|-------------|
| `/ws` | Connect | WebSocket endpoint (with SockJS fallback) |
| `/app/chat.sendMessage/{roomId}` | Client→Server | Send message to room |
| `/app/chat.privateMessage` | Client→Server | Send private message |
| `/app/chat.typing` | Client→Server | Typing indicator |
| `/topic/room/{roomId}` | Server→Client | Receive room messages |
| `/topic/room/{roomId}/typing` | Server→Client | Receive typing indicators |
| `/topic/presence` | Server→Client | Presence updates |
| `/user/queue/messages` | Server→Client | Private messages |
| `/topic/public` | Server→Client | Public chat |

## REST API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/rooms?createdBy=` | Create chat room |
| GET | `/api/rooms` | List public rooms |
| GET | `/api/rooms/{id}` | Get room by ID |
| GET | `/api/rooms/by-name/{name}` | Get room by name |
| DELETE | `/api/rooms/{id}` | Delete room |
| POST | `/api/messages?sender=` | Send message |
| GET | `/api/messages/room/{roomId}` | Get message history |
| GET | `/api/messages/room/{roomId}/recent` | Get recent messages |
| POST | `/api/messages/{messageId}/read` | Mark message as read |
| POST | `/api/presence/register?username=` | Register user |
| GET | `/api/presence/online` | Get online users |
| GET | `/api/presence/online/count` | Get online count |
| GET | `/api/presence/{username}` | Check user online |
