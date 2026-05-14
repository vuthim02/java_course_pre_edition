# Chat Application — Complete Java Source Code

---

## `ChatServer.java`

```java
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Multi-threaded chat server.
 * Accepts connections on port 12345, manages chat rooms,
 * broadcasts messages, and keeps per-room message history.
 */
public class ChatServer {

    private static final int PORT = 12345;

    // room name  ->  list of connected handlers
    private static final Map<String, List<ClientHandler>> rooms = new ConcurrentHashMap<>();
    // room name  ->  list of historical messages
    private static final Map<String, List<String>> messageHistory = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server starting on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running. Press Ctrl+C to stop.");
            while (true) {
                // Accept a new client and hand it off to a handler thread
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    //  Room management  (synchronized to avoid race conditions)
    // ---------------------------------------------------------------

    /** Adds a client to a room and sends them the recent history */
    public static synchronized void joinRoom(String room, ClientHandler client) {
        rooms.computeIfAbsent(room, k -> new CopyOnWriteArrayList<>()).add(client);
        broadcast(room, client.getUsername() + " joined the room.");

        // Send message history to the newcomer
        List<String> history = messageHistory.get(room);
        if (history != null && !history.isEmpty()) {
            client.sendMessage("--- Room history (" + room + ") ---");
            for (String msg : history) {
                client.sendMessage(msg);
            }
            client.sendMessage("--- End of history ---");
        }
    }

    /** Removes a client from a room; cleans up the room if empty */
    public static synchronized void leaveRoom(String room, ClientHandler client) {
        List<ClientHandler> clients = rooms.get(room);
        if (clients != null) {
            clients.remove(client);
            if (clients.isEmpty()) {
                rooms.remove(room);
            }
        }
        broadcast(room, client.getUsername() + " left the room.");
    }

    /** Sends a message to every client in a room and saves it to history */
    public static synchronized void broadcast(String room, String message) {
        List<ClientHandler> clients = rooms.get(room);
        if (clients == null) return;

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formatted = "[" + timestamp + "] " + message;

        // Keep last 200 messages per room
        messageHistory.computeIfAbsent(room, k -> new CopyOnWriteArrayList<>()).add(formatted);
        List<String> history = messageHistory.get(room);
        if (history.size() > 200) {
            history.remove(0);
        }

        for (ClientHandler client : clients) {
            client.sendMessage(formatted);
        }
    }

    /** Returns the list of active room names */
    public static List<String> getRooms() {
        return List.copyOf(rooms.keySet());
    }

    /** Returns the clients in a given room (for the /users command) */
    public static List<ClientHandler> getRoomUsers(String room) {
        List<ClientHandler> clients = rooms.get(room);
        return clients == null ? List.of() : List.copyOf(clients);
    }
}

/**
 * Handles communication with a single client on its own thread.
 */
class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private String currentRoom;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Set up I/O streams
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Get username
            out.println("Enter your username:");
            username = in.readLine();
            if (username == null || username.isBlank()) {
                username = "Anonymous";
            }
            out.println("Welcome, " + username + "!");
            out.println("Commands: /join <room>, /leave, /rooms, /users, /quit");

            // Main message loop
            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("/")) {
                    handleCommand(input);
                } else if (currentRoom != null) {
                    ChatServer.broadcast(currentRoom, username + ": " + input);
                } else {
                    out.println("You are not in a room. Use /join <room> to join one.");
                }
            }
        } catch (IOException e) {
            // Client disconnected
        } finally {
            cleanup();
        }
    }

    /** Parses and executes a slash command */
    private void handleCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/join" -> {
                if (parts.length < 2) {
                    out.println("Usage: /join <roomname>");
                    return;
                }
                String room = parts[1].trim();
                // Leave current room if any
                if (currentRoom != null) {
                    ChatServer.leaveRoom(currentRoom, this);
                }
                currentRoom = room;
                ChatServer.joinRoom(room, this);
            }
            case "/leave" -> {
                if (currentRoom != null) {
                    ChatServer.leaveRoom(currentRoom, this);
                    currentRoom = null;
                } else {
                    out.println("You are not in a room.");
                }
            }
            case "/rooms" -> {
                List<String> roomList = ChatServer.getRooms();
                if (roomList.isEmpty()) {
                    out.println("No active rooms.");
                } else {
                    out.println("Active rooms: " + String.join(", ", roomList));
                }
            }
            case "/users" -> {
                if (currentRoom == null) {
                    out.println("You are not in a room.");
                    return;
                }
                out.println("Users in " + currentRoom + ":");
                for (ClientHandler h : ChatServer.getRoomUsers(currentRoom)) {
                    out.println("  " + h.getUsername());
                }
            }
            case "/quit" -> cleanup();
            default -> out.println("Commands: /join <room>, /leave, /rooms, /users, /quit");
        }
    }

    /** Cleans up: leaves room, closes socket */
    private void cleanup() {
        if (currentRoom != null) {
            ChatServer.leaveRoom(currentRoom, this);
            currentRoom = null;
        }
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException ignored) { }
    }

    // --- Public API used by ChatServer ---

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }
}
```

---

## `ChatClient.java`

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Console-based chat client.
 * Connects to ChatServer on port 12345.
 * Uses two threads: one reads from the network, the other reads keyboard input.
 */
public class ChatClient {

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);

        System.out.print("Server address (default: localhost): ");
        String host = console.nextLine().trim();
        if (host.isEmpty()) host = "localhost";

        System.out.print("Port (default: 12345): ");
        String portStr = console.nextLine().trim();
        int port = portStr.isEmpty() ? 12345 : Integer.parseInt(portStr);

        try (Socket socket = new Socket(host, port)) {
            PrintWriter out   = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // Thread: continuously read messages from the server and print them
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    // Server disconnected
                }
            });
            reader.setDaemon(true);  // JVM can exit when main thread ends
            reader.start();

            // Main thread: read keyboard input and send to server
            String input;
            while ((input = console.nextLine()) != null) {
                out.println(input);
                if (input.equalsIgnoreCase("/quit")) break;
            }

            // Graceful shutdown
            System.out.println("Disconnecting...");
        } catch (ConnectException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            console.close();
        }
    }
}
```
