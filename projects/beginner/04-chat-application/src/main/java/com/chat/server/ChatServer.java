package com.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private final ServerSocket serverSocket;
    private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running = true;

    public ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("[SERVER] Chat server started on port " + serverSocket.getLocalPort());
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] New connection from " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                synchronized (clients) {
                    clients.add(handler);
                }
                pool.execute(handler);
            } catch (IOException e) {
                if (running) {
                    System.err.println("[SERVER] Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public ChatRoom getOrCreateRoom(String name) {
        return rooms.computeIfAbsent(name.toLowerCase(), k -> new ChatRoom(name));
    }

    public ChatRoom getRoom(String name) {
        return rooms.get(name.toLowerCase());
    }

    public ChatRoom createRoom(String name) {
        String key = name.toLowerCase();
        ChatRoom room = new ChatRoom(name);
        ChatRoom existing = rooms.putIfAbsent(key, room);
        return existing == null ? room : null;
    }

    public void broadcastToRoom(ChatRoom room, String message, ClientHandler exclude) {
        for (ClientHandler client : room.getUsers()) {
            if (client != exclude) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
        pool.shutdownNow();
    }

    public static void main(String[] args) {
        try {
            new ChatServer(12345).start();
        } catch (IOException e) {
            System.err.println("[SERVER] Failed to start: " + e.getMessage());
        }
    }
}
