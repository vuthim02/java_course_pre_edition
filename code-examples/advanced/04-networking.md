# Networking, Sockets, and HTTP

This document covers Java networking including `InetAddress`, `URL`/`URLConnection`, `HttpURLConnection`, TCP socket programming with `ServerSocket`/`Socket`, the Java 11+ `HttpClient`, and a simple echo server.

## InetAddress

```java
import java.net.*;

public class InetAddressDemo {
    public static void main(String[] args) throws UnknownHostException {
        System.out.println("--- InetAddress ---");

        // Get by hostname
        InetAddress address = InetAddress.getByName("google.com");
        System.out.println("Host: " + address.getHostName());
        System.out.println("IP: " + address.getHostAddress());
        System.out.println("Canonical: " + address.getCanonicalHostName());

        // Local host
        InetAddress local = InetAddress.getLocalHost();
        System.out.println("\nLocal host: " + local.getHostName() + " (" + local.getHostAddress() + ")");

        // Loopback
        InetAddress loopback = InetAddress.getLoopbackAddress();
        System.out.println("Loopback: " + loopback);

        // Reachability test (may block, may return false due to firewalls)
        try {
            System.out.println("Reachable (5s timeout): " + address.isReachable(5000));
        } catch (Exception e) {
            System.out.println("Reachability test failed: " + e.getMessage());
        }

        // Get all addresses for a host
        InetAddress[] all = InetAddress.getAllByName("google.com");
        System.out.println("\nAll addresses for google.com:");
        for (InetAddress a : all) {
            System.out.println("  " + a.getHostAddress());
        }
    }
}
```

## URL and URLConnection

```java
import java.io.*;
import java.net.*;

public class URLDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- URL / URLConnection (HTTP GET) ---");

        URL url = new URL("https://httpbin.org/get");
        System.out.println("Protocol: " + url.getProtocol());
        System.out.println("Host: " + url.getHost());
        System.out.println("Port: " + url.getPort()); // -1 if not specified
        System.out.println("Path: " + url.getPath());
        System.out.println("Query: " + url.getQuery());

        // Open connection and read response
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Accept", "text/plain");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            System.out.println("\nResponse:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
        }
    }
}
```

## HttpURLConnection (GET, POST, Headers)

```java
import java.io.*;
import java.net.*;

public class HttpURLConnectionDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- HttpURLConnection (GET) ---");
        httpGet("https://httpbin.org/get?name=Java");

        System.out.println("\n--- HttpURLConnection (POST) ---");
        httpPost("https://httpbin.org/post", "{\"key\":\"value\"}");
    }

    static void httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Java-Demo/1.0");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode + " " + conn.getResponseMessage());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
        }
        conn.disconnect();
    }

    static void httpPost(String urlStr, String jsonBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true); // needed for POST

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
        }
        conn.disconnect();
    }
}
```

## Simple TCP Echo Server and Client

```java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        int port = 7777;
        System.out.println("Echo server starting on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on " + serverSocket.getLocalSocketAddress());

            // Handle one client (for demo simplicity)
            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

                var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                var out = new PrintWriter(clientSocket.getOutputStream(), true);

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Received: " + line);
                    out.println("Echo: " + line);
                    if ("bye".equalsIgnoreCase(line.trim())) {
                        break;
                    }
                }
                System.out.println("Client disconnected.");
            }
        }
    }
}
```

```java
import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 7777;

        System.out.println("Connecting to echo server at " + host + ":" + port);
        try (Socket socket = new Socket(host, port);
             var out = new PrintWriter(socket.getOutputStream(), true);
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected. Type messages (type 'bye' to quit):");
            String userInput;
            while ((userInput = console.readLine()) != null) {
                out.println(userInput);
                String response = in.readLine();
                System.out.println("Server: " + response);
                if ("bye".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
            }
        }
    }
}
```

## Multi-Threaded Server (Thread Per Connection)

```java
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MultiThreadedServer {
    public static void main(String[] args) throws IOException {
        int port = 7778;
        // Thread pool for handling connections
        ExecutorService pool = Executors.newFixedThreadPool(4);
        System.out.println("Multi-threaded server on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Accept 3 clients then stop (for demo)
            for (int i = 0; i < 3; i++) {
                Socket clientSocket = serverSocket.accept();
                pool.submit(new ClientHandler(clientSocket));
            }
        }
        pool.shutdown();
        System.out.println("Server shut down.");
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (socket;
             var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("Hello from thread " + Thread.currentThread().getName());
            String line;
            while ((line = in.readLine()) != null) {
                out.println("Echo: " + line);
                if ("bye".equalsIgnoreCase(line.trim())) break;
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        }
    }
}
```

## Java 11+ HttpClient

```java
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;

public class HttpClientDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Java 11+ HttpClient ---");

        // Create client (reusable)
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        // Synchronous GET
        System.out.println("--- Synchronous GET ---");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get"))
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body (first 200 chars): " + response.body().substring(0, Math.min(200, response.body().length())));

        // Asynchronous GET
        System.out.println("\n--- Asynchronous GET ---");
        CompletableFuture<HttpResponse<String>> futureResponse =
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        futureResponse.thenAccept(resp -> {
            System.out.println("Async status: " + resp.statusCode());
            System.out.println("Async body (first 200): " + resp.body().substring(0, Math.min(200, resp.body().length())));
        }).join(); // wait for completion

        // POST with JSON body
        System.out.println("\n--- POST with JSON ---");
        String json = """
            {"name": "Java", "type": "programming language"}
            """;
        HttpRequest postRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/post"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST status: " + postResponse.statusCode());
        System.out.println("POST body (first 200): " + postResponse.body().substring(0, Math.min(200, postResponse.body().length())));
    }
}
```
