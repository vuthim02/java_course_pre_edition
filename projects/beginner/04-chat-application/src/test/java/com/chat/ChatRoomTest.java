package com.chat;

import com.chat.server.ChatRoom;
import com.chat.server.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatRoomTest {

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        chatRoom = new ChatRoom("test-room");
    }

    @Nested
    class MessageHistory {

        @Test
        void testAddMessage() {
            chatRoom.addMessage("Hello World");

            List<String> history = chatRoom.getMessageHistory();
            assertEquals(1, history.size());
            assertEquals("Hello World", history.get(0));
        }

        @Test
        void testMessageHistoryBoundedTo100() {
            for (int i = 0; i < 150; i++) {
                chatRoom.addMessage("Message " + i);
            }

            List<String> history = chatRoom.getMessageHistory();
            assertEquals(100, history.size());
            assertEquals("Message 50", history.get(0));
        }

        @Test
        void testMessageHistoryReturnsCopy() {
            chatRoom.addMessage("Test");
            List<String> history = chatRoom.getMessageHistory();
            assertThrows(UnsupportedOperationException.class, () -> history.add("illegal"));
        }

        @Test
        void testInitialHistoryEmpty() {
            assertTrue(chatRoom.getMessageHistory().isEmpty());
        }

        @Test
        void testMultipleMessages() {
            chatRoom.addMessage("First");
            chatRoom.addMessage("Second");
            chatRoom.addMessage("Third");

            List<String> history = chatRoom.getMessageHistory();
            assertEquals(3, history.size());
            assertEquals("First", history.get(0));
            assertEquals("Third", history.get(2));
        }
    }

    @Nested
    class UserManagement {

        @Test
        void testAddUser() {
            ChatRoom room = new ChatRoom("lobby");
            ClientHandler mockUser = new MockClientHandler("Alice");

            room.addUser(mockUser);

            assertEquals(1, room.getUserCount());
        }

        @Test
        void testRemoveUser() {
            ChatRoom room = new ChatRoom("lobby");
            ClientHandler mockUser = new MockClientHandler("Alice");
            room.addUser(mockUser);

            room.removeUser(mockUser);

            assertEquals(0, room.getUserCount());
        }

        @Test
        void testGetUsersSet() {
            ChatRoom room = new ChatRoom("lobby");
            ClientHandler alice = new MockClientHandler("Alice");
            ClientHandler bob = new MockClientHandler("Bob");

            room.addUser(alice);
            room.addUser(bob);

            assertEquals(2, room.getUsers().size());
        }

        @Test
        void testRemoveNonExistentUser() {
            ChatRoom room = new ChatRoom("lobby");
            ClientHandler mockUser = new MockClientHandler("Ghost");

            room.removeUser(mockUser);

            assertEquals(0, room.getUserCount());
        }

        @Test
        void testConcurrentUserAdditions() {
            ChatRoom room = new ChatRoom("lobby");
            for (int i = 0; i < 10; i++) {
                room.addUser(new MockClientHandler("User" + i));
            }
            assertEquals(10, room.getUserCount());
        }
    }

    @Nested
    class RoomProperties {

        @Test
        void testGetName() {
            assertEquals("test-room", chatRoom.getName());
        }

        @Test
        void testGetUserCount_Empty() {
            assertEquals(0, chatRoom.getUserCount());
        }

        @Test
        void testGetUserCount_AfterAddAndRemove() {
            ClientHandler user = new MockClientHandler("Alice");
            chatRoom.addUser(user);
            assertEquals(1, chatRoom.getUserCount());

            chatRoom.removeUser(user);
            assertEquals(0, chatRoom.getUserCount());
        }
    }

    private static class MockClientHandler extends ClientHandler {
        private final String username;

        protected MockClientHandler(String username) {
            super(null, null);
            this.username = username;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public void sendMessage(String message) {
        }
    }
}
