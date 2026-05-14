# Core Java Advanced — Lesson 4: Networking

## Networking Basics

Java makes network programming accessible through `java.net`:

```
Client                          Server
  │                               │
  │────── Connection Request ────▶│
  │                               │
  │◀───── Accept Connection ─────│
  │                               │
  │────── Send Data ────────────▶│  (OutputStream)
  │◀───── Receive Data ──────────│  (InputStream)
  │                               │
  │────── Close ────────────────▶│
```

## TCP Socket Programming

### TCP Server

```java
import java.io.*;
import java.net.*;

public class SimpleServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                // Accept a client connection (BLOCKS until client connects)
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Client connected: " +
                        clientSocket.getInetAddress().getHostAddress());

                    // Read from client
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                    String message = in.readLine();
                    System.out.println("Received: " + message);

                    // Send response
                    PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(), true);
                    out.println("Echo: " + message);
                }
            }
        }
    }
}
```

### TCP Client

```java
import java.io.*;
import java.net.*;

public class SimpleClient {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to server");

            // Send data
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);
            out.println("Hello, Server!");

            // Receive response
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            System.out.println("Server response: " + response);
        }
    }
}
```

### Multi-Threaded Server

```java
public class MultiThreadedServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Multi-threaded server on port 8080");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            // Handle each client in a new thread
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (clientSocket) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("From " +
                    clientSocket.getInetAddress() + ": " + inputLine);
                out.println("Echo: " + inputLine);
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        }
    }
}
```

## UDP (Datagram) — Connectionless

```java
// UDP Server
public class UDPServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(9876);
        byte[] buffer = new byte[1024];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);  // Blocking

            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + received);

            // Send response
            String response = "Echo: " + received;
            byte[] responseData = response.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(
                responseData, responseData.length,
                packet.getAddress(), packet.getPort());
            socket.send(responsePacket);
        }
    }
}

// UDP Client
public class UDPClient {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");

        byte[] sendData = "Hello, UDP!".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(
            sendData, sendData.length, address, 9876);
        socket.send(sendPacket);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(
            receiveData, receiveData.length);
        socket.receive(receivePacket);
        System.out.println("Response: " +
            new String(receivePacket.getData(), 0, receivePacket.getLength()));
    }
}
```

## HTTP Connections (Java 11+ HttpClient)

```java
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class HttpClientExample {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        // GET request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/users/octocat"))
            .header("Accept", "application/json")
            .GET()
            .build();

        // Synchronous
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());

        // Asynchronous
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(res -> System.out.println("Async: " + res.body()));

        Thread.sleep(1000);  // Wait for async
    }
}
```

## URL and URLConnection

```java
URL url = new URL("https://example.com/api/data");
URLConnection conn = url.openConnection();
conn.setRequestProperty("Accept", "application/json");

try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(conn.getInputStream()))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}
```

## InetAddress

```java
InetAddress address = InetAddress.getByName("google.com");
System.out.println("Host: " + address.getHostName());
System.out.println("IP: " + address.getHostAddress());

InetAddress localHost = InetAddress.getLocalHost();
System.out.println("My IP: " + localHost.getHostAddress());

// Check reachability
System.out.println("Reachable: " + address.isReachable(5000));
```

---

### Exercises

1. Build a simple echo server and client (client sends message, server echoes it back).
2. Build a multi-client chat server using threads (one thread per client).
3. Write a simple HTTP server that serves static files.
4. Use the Java 11+ HttpClient to fetch data from a public API (e.g., JSONPlaceholder, GitHub API).
5. Build a UDP-based ping-pong application.
