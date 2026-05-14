package com.chat;

import com.chat.controller.ChatController;
import com.chat.dto.ChatMessageRequest;
import com.chat.dto.ChatMessageResponse;
import com.chat.model.ChatRoom;
import com.chat.service.ChatService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    private ChatMessageResponse createResponse(Long id, String roomId, String sender, String content) {
        return ChatMessageResponse.builder()
                .id(id)
                .roomId(roomId)
                .sender(sender)
                .content(content)
                .type("CHAT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    class WebSocketEndpoints {

        @Test
        void testSendMessageWebSocket() {
            ChatMessageRequest request = ChatMessageRequest.builder()
                    .roomId("room1").sender("Alice").content("Hello").type("CHAT").build();
            ChatMessageResponse response = createResponse(1L, "room1", "Alice", "Hello");
            when(chatService.saveAndPublish(any(ChatMessageRequest.class))).thenReturn(response);

            SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
            ChatController controller = new ChatController(chatService, template);
            controller.sendMessage(request);

            verify(template).convertAndSend(eq("/topic/room/room1"), eq(response));
        }

        @Test
        void testJoinRoom() {
            ChatMessageRequest request = ChatMessageRequest.builder()
                    .roomId("room1").sender("Alice").build();
            when(chatService.saveMessage(any())).thenReturn(createResponse(1L, "room1", "Alice", "Alice joined the room"));

            SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
            ChatController controller = new ChatController(chatService, template);
            controller.joinRoom(request);

            verify(template).convertAndSend(eq("/topic/room/room1"), any(ChatMessageResponse.class));
        }

        @Test
        void testLeaveRoom() {
            ChatMessageRequest request = ChatMessageRequest.builder()
                    .roomId("room1").sender("Alice").build();
            when(chatService.saveMessage(any())).thenReturn(createResponse(1L, "room1", "Alice", "Alice left the room"));

            SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
            ChatController controller = new ChatController(chatService, template);
            controller.leaveRoom(request);

            verify(template).convertAndSend(eq("/topic/room/room1"), any(ChatMessageResponse.class));
        }

        @Test
        void testTyping() {
            ChatMessageRequest request = ChatMessageRequest.builder()
                    .roomId("room1").sender("Alice").build();

            SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
            ChatController controller = new ChatController(chatService, template);
            controller.typing(request);

            verify(template).convertAndSend(eq("/topic/room/room1/typing"), eq("Alice is typing..."));
        }
    }

    @Nested
    class RestEndpoints {

        @Test
        void testGetAllRooms() throws Exception {
            ChatRoom room1 = ChatRoom.builder().id(1L).roomId("room1").name("General").build();
            ChatRoom room2 = ChatRoom.builder().id(2L).roomId("room2").name("Random").build();
            when(chatService.getAllRooms()).thenReturn(List.of(room1, room2));

            mockMvc.perform(get("/api/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].roomId").value("room1"))
                    .andExpect(jsonPath("$[1].roomId").value("room2"));
        }

        @Test
        void testGetAllRooms_Empty() throws Exception {
            when(chatService.getAllRooms()).thenReturn(List.of());

            mockMvc.perform(get("/api/rooms"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void testCreateRoom() throws Exception {
            ChatRoom room = ChatRoom.builder().id(1L).roomId("newroom").name("New Room").build();
            when(chatService.createRoom("newroom", "New Room")).thenReturn(room);

            mockMvc.perform(post("/api/rooms")
                            .param("roomId", "newroom")
                            .param("name", "New Room"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.roomId").value("newroom"))
                    .andExpect(jsonPath("$.name").value("New Room"));
        }

        @Test
        void testCreateRoom_Duplicate() throws Exception {
            when(chatService.createRoom("existing", "Existing"))
                    .thenThrow(new RuntimeException("Room already exists"));

            mockMvc.perform(post("/api/rooms")
                            .param("roomId", "existing")
                            .param("name", "Existing"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void testGetMessages() throws Exception {
            Page<ChatMessageResponse> page = new PageImpl<>(List.of(
                    createResponse(1L, "room1", "Alice", "Hello"),
                    createResponse(2L, "room1", "Bob", "Hi")));
            when(chatService.getMessages(eq("room1"), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(get("/api/rooms/room1/messages")
                            .param("page", "0")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.size()").value(2))
                    .andExpect(jsonPath("$.content[0].sender").value("Alice"));
        }

        @Test
        void testGetMessages_Empty() throws Exception {
            when(chatService.getMessages(eq("emptyroom"), anyInt(), anyInt())).thenReturn(Page.empty());

            mockMvc.perform(get("/api/rooms/emptyroom/messages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        void testGetHistory() throws Exception {
            List<com.chat.model.ChatMessage> history = List.of(
                    com.chat.model.ChatMessage.builder().id(1L).roomId("room1").sender("Alice").content("Hello").type("CHAT").build(),
                    com.chat.model.ChatMessage.builder().id(2L).roomId("room1").sender("Bob").content("Hi").type("CHAT").build());
            when(chatService.getChatHistory("room1")).thenReturn(history);

            mockMvc.perform(get("/api/rooms/room1/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].sender").value("Alice"));
        }

        @Test
        void testGetHistory_Empty() throws Exception {
            when(chatService.getChatHistory("emptyroom")).thenReturn(List.of());

            mockMvc.perform(get("/api/rooms/emptyroom/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}
