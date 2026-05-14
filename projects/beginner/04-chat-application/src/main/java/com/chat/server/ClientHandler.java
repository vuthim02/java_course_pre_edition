package com.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;
    private final BufferedReader in;
    private final PrintWriter out;
    private String username;
    private ChatRoom currentRoom;

    public ClientHandler(Socket socket, ChatServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            out.println("Enter your username:");
            username = in.readLine();
            if (username == null || username.isBlank()) {
                username = "Anonymous";
            }

            currentRoom = server.getOrCreateRoom("lobby");
            currentRoom.addUser(this);
            currentRoom.addMessage("[SERVER] " + username + " joined the lobby");
            server.broadcastToRoom(currentRoom, "[SERVER] " + username + " joined the lobby", this);
            sendMessage("[SERVER] Welcome, " + username + "! Type /help for commands.");

            List<String> history = currentRoom.getMessageHistory();
            if (!history.isEmpty()) {
                sendMessage("[SERVER] --- Message History ---");
                for (String msg : history) {
                    sendMessage(msg);
                }
                sendMessage("[SERVER] --- End of History ---");
            }

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("/")) {
                    handleCommand(line);
                } else if (currentRoom != null) {
                    String formatted = username + ": " + line;
                    currentRoom.addMessage(formatted);
                    server.broadcastToRoom(currentRoom, formatted, null);
                } else {
                    sendMessage("[SERVER] You are not in a room. Use /join or /create.");
                }
            }
        } catch (IOException e) {
            // client disconnected
        } finally {
            disconnect();
        }
    }

    private void handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1] : "";

        switch (cmd) {
            case "/join" -> handleJoin(arg);
            case "/create" -> handleCreate(arg);
            case "/leave" -> handleLeave();
            case "/users" -> handleUsers();
            case "/quit" -> handleQuit();
            case "/help" -> handleHelp();
            default -> sendMessage("[SERVER] Unknown command: " + cmd + ". Type /help for commands.");
        }
    }

    private void handleJoin(String roomName) {
        if (roomName.isBlank()) {
            sendMessage("[SERVER] Usage: /join <roomname>");
            return;
        }
        ChatRoom room = server.getRoom(roomName);
        if (room == null) {
            sendMessage("[SERVER] Room '" + roomName + "' does not exist. Use /create to create it.");
            return;
        }
        if (currentRoom != null) {
            currentRoom.removeUser(this);
            currentRoom.addMessage("[SERVER] " + username + " left the room");
            server.broadcastToRoom(currentRoom, "[SERVER] " + username + " left the room", this);
        }
        currentRoom = room;
        currentRoom.addUser(this);
        currentRoom.addMessage("[SERVER] " + username + " joined the room");
        server.broadcastToRoom(currentRoom, "[SERVER] " + username + " joined the room", this);
        sendMessage("[SERVER] Joined room '" + roomName + "'");

        List<String> history = currentRoom.getMessageHistory();
        if (!history.isEmpty()) {
            sendMessage("[SERVER] --- Message History ---");
            for (String msg : history) {
                sendMessage(msg);
            }
            sendMessage("[SERVER] --- End of History ---");
        }
    }

    private void handleCreate(String roomName) {
        if (roomName.isBlank()) {
            sendMessage("[SERVER] Usage: /create <roomname>");
            return;
        }
        ChatRoom room = server.createRoom(roomName);
        if (room == null) {
            sendMessage("[SERVER] Room '" + roomName + "' already exists.");
            return;
        }
        if (currentRoom != null) {
            currentRoom.removeUser(this);
            currentRoom.addMessage("[SERVER] " + username + " left the room");
            server.broadcastToRoom(currentRoom, "[SERVER] " + username + " left the room", this);
        }
        currentRoom = room;
        currentRoom.addUser(this);
        sendMessage("[SERVER] Created and joined room '" + roomName + "'");
    }

    private void handleLeave() {
        if (currentRoom == null) {
            sendMessage("[SERVER] You are not in a room.");
            return;
        }
        String roomName = currentRoom.getName();
        currentRoom.removeUser(this);
        currentRoom.addMessage("[SERVER] " + username + " left the room");
        server.broadcastToRoom(currentRoom, "[SERVER] " + username + " left the room", this);
        currentRoom = null;
        sendMessage("[SERVER] Left room '" + roomName + "'");
    }

    private void handleUsers() {
        if (currentRoom == null) {
            sendMessage("[SERVER] You are not in a room.");
            return;
        }
        StringBuilder sb = new StringBuilder("[SERVER] Users in '" + currentRoom.getName() + "': ");
        boolean first = true;
        for (ClientHandler user : currentRoom.getUsers()) {
            if (!first) sb.append(", ");
            sb.append(user.getUsername());
            first = false;
        }
        sendMessage(sb.toString());
    }

    private void handleQuit() {
        sendMessage("[SERVER] Goodbye!");
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void handleHelp() {
        sendMessage("[SERVER] --- Commands ---");
        sendMessage("/join <room>   - Join an existing room");
        sendMessage("/create <room> - Create a new room");
        sendMessage("/leave         - Leave current room");
        sendMessage("/users         - List users in current room");
        sendMessage("/quit          - Disconnect");
        sendMessage("/help          - Show this help");
        sendMessage("[SERVER] -----------------");
    }

    private void disconnect() {
        if (currentRoom != null) {
            currentRoom.removeUser(this);
            currentRoom.addMessage("[SERVER] " + username + " disconnected");
            server.broadcastToRoom(currentRoom, "[SERVER] " + username + " disconnected", this);
        }
        server.removeClient(this);
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
