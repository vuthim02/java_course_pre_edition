package com.chat.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {

    private final String name;
    private final Set<ClientHandler> users = ConcurrentHashMap.newKeySet();
    private final List<String> messageHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 100;

    public ChatRoom(String name) {
        this.name = name;
    }

    public synchronized void addMessage(String message) {
        messageHistory.add(message);
        if (messageHistory.size() > MAX_HISTORY) {
            messageHistory.remove(0);
        }
    }

    public synchronized List<String> getMessageHistory() {
        return List.copyOf(messageHistory);
    }

    public void addUser(ClientHandler user) {
        users.add(user);
    }

    public void removeUser(ClientHandler user) {
        users.remove(user);
    }

    public Set<ClientHandler> getUsers() {
        return users;
    }

    public String getName() {
        return name;
    }

    public int getUserCount() {
        return users.size();
    }
}
