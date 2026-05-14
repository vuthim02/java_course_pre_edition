package com.chat;

import com.chat.server.ChatRoom;
import com.chat.server.ChatServer;
import com.chat.server.ClientHandler;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

class ChatServerTest {

    private ChatServer chatServer;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }
        chatServer = new ChatServer(port);
    }

    @AfterEach
    void tearDown() {
        chatServer.stop();
    }

    @Nested
    class RoomCreation {

        @Test
        void testCreateRoom() {
            ChatRoom room = chatServer.createRoom("general");

            assertNotNull(room);
            assertEquals("general", room.getName());
        }

        @Test
        void testCreateDuplicateRoomReturnsNull() {
            chatServer.createRoom("general");

            ChatRoom duplicate = chatServer.createRoom("general");

            assertNull(duplicate);
        }

        @Test
        void testCreateRoomCaseInsensitive() {
            chatServer.createRoom("General");

            assertNull(chatServer.createRoom("general"));
        }

        @Test
        void testGetOrCreateRoom() {
            ChatRoom room = chatServer.getOrCreateRoom("lobby");

            assertNotNull(room);
            assertEquals("lobby", room.getName());
        }

        @Test
        void testGetOrCreateRoom_Existing() {
            chatServer.createRoom("lobby");

            ChatRoom room = chatServer.getOrCreateRoom("lobby");

            assertNotNull(room);
            assertEquals("lobby", room.getName());
        }

        @Test
        void testGetRoom_NonExistent() {
            ChatRoom room = chatServer.getRoom("nonexistent");

            assertNull(room);
        }

        @Test
        void testGetRoom_Existing() {
            chatServer.createRoom("test-room");

            ChatRoom room = chatServer.getRoom("test-room");

            assertNotNull(room);
            assertEquals("test-room", room.getName());
        }

        @Test
        void testGetRoomCaseInsensitive() {
            chatServer.createRoom("MyRoom");

            assertNotNull(chatServer.getRoom("myroom"));
            assertNotNull(chatServer.getRoom("MYROOM"));
        }
    }

    @Nested
    class MessageRouting {

        @Test
        void testBroadcastToRoom() {
            ChatRoom room = chatServer.createRoom("test");
            TestClientHandler client1 = new TestClientHandler();
            TestClientHandler client2 = new TestClientHandler();
            room.addUser(client1);
            room.addUser(client2);

            chatServer.broadcastToRoom(room, "Hello all", null);

            assertEquals("Hello all", client1.lastMessage);
            assertEquals("Hello all", client2.lastMessage);
        }

        @Test
        void testBroadcastExcludesSender() {
            ChatRoom room = chatServer.createRoom("test");
            TestClientHandler sender = new TestClientHandler();
            TestClientHandler other = new TestClientHandler();
            room.addUser(sender);
            room.addUser(other);

            chatServer.broadcastToRoom(room, "Secret", sender);

            assertNull(sender.lastMessage);
            assertEquals("Secret", other.lastMessage);
        }

        @Test
        void testBroadcastToEmptyRoom() {
            ChatRoom room = chatServer.createRoom("empty");

            assertDoesNotThrow(() -> chatServer.broadcastToRoom(room, "Hello", null));
        }
    }

    private static class TestClientHandler extends ClientHandler {
        String lastMessage;

        protected TestClientHandler() {
            super(null, null);
        }

        @Override
        public void sendMessage(String message) {
            this.lastMessage = message;
        }

        @Override
        public String getUsername() {
            return "test-user";
        }
    }
}
