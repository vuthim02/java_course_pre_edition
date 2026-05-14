# Domain Specialization — Lesson 6: Networking & WebSocket Servers

> **INTRODUCTORY OVERVIEW** — This section provides a high-level introduction to the domain. Each topic warrants its own dedicated course for professional mastery.

## Why WebSockets?

HTTP is **request-response** — the client asks, the server answers. But what if the server needs to send data WITHOUT being asked? Chat messages, stock prices, game state, notifications.

```
HTTP (Polling):                      WebSocket (Persistent):
┌──────────┐     ┌──────────┐       ┌──────────┐     ┌──────────┐
│ Client   │────▶│ Server   │       │ Client   │═════│ Server   │
│          │     │          │       │          │     │          │
│ GET /data │◀────│ Response │       │ ───msg──▶│     │          │
│          │     │          │       │          │     │          │
│ GET /data │────▶│          │       │ ◀───msg──│     │          │
│          │◀────│ Response │       │          │     │          │
│          │     │          │       │ ───msg──▶│     │          │
│ GET /data │────▶│          │       │ ◀───msg──│     │          │
│          │◀────│ Response │       │ ◀───msg──│     │          │
│          │     │          │       │          │     │          │
│ ❌ 1 msg = 3 HTTP overhead   │     │ ✅ 1 TCP connection       │
│ ❌ Up to 30s delay           │     │ ✅ Real-time (ms delay)   │
└──────────────────────────────┘     └──────────────────────────┘
```

## WebSocket with Spring Boot

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");   // Broadcast to subscribers
        config.setApplicationDestinationPrefixes("/app");  // Client→Server
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")      // WebSocket handshake endpoint
            .setAllowedOrigins("*")
            .withSockJS();              // Fallback for older browsers
    }
}
```

### Controller

```java
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Client sends to /app/chat → handled here
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage handleChat(ChatMessage message, @DestinationVariable String roomId) {
        // Broadcast to ALL subscribers of /topic/chat/{roomId}
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    // Server-initiated message (not in response to client)
    public void sendNotification(String userId, Notification notification) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/topic/notifications",
            notification
        );
    }
}

// Message POJO
public record ChatMessage(
    String sender,
    String content,
    long timestamp
) {}
```

### JavaScript Client

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);

    // Subscribe to room
    stompClient.subscribe('/topic/chat/room1', function(message) {
        const chat = JSON.parse(message.body);
        displayMessage(chat.sender, chat.content);
    });

    // Receive private notifications
    stompClient.subscribe('/user/topic/notifications', function(message) {
        showNotification(JSON.parse(message.body));
    });
});

function sendMessage() {
    stompClient.send("/app/chat/room1", {}, JSON.stringify({
        sender: userId,
        content: messageInput.value
    }));
}
```

## Chat Server (Netty/NIO)

For a high-performance custom WebSocket server without Spring:

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.100.Final</version>
</dependency>
```

```java
public class ChatServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                            new HttpServerCodec(),
                            new HttpObjectAggregator(65536),
                            new WebSocketServerProtocolHandler("/chat"),
                            new ChatHandler()
                        );
                    }
                });

            ChannelFuture future = bootstrap.bind(8080).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

// Handler
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final List<Channel> channels = new ArrayList<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
        broadcast("User joined (" + channels.size() + " online)");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        broadcast(msg.text());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channels.remove(ctx.channel());
        broadcast("User left (" + channels.size() + " online)");
    }

    private void broadcast(String message) {
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        for (Channel channel : channels) {
            channel.writeAndFlush(frame.retain());
        }
    }
}
```

## Socket Programming (Raw TCP/UDP)

### TCP Server

```java
public class TcpServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server listening on port 8080");

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private static void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(
                client.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);
                out.println("Echo: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### UDP Server

```java
public class UdpServer {

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(8080);
        byte[] buffer = new byte[1024];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + received);

            String response = "Echo: " + received;
            DatagramPacket responsePacket = new DatagramPacket(
                response.getBytes(), response.length(),
                packet.getAddress(), packet.getPort()
            );
            socket.send(responsePacket);
        }
    }
}
```

## TCP vs UDP vs WebSocket

| Protocol | Connection | Ordering | Speed | Use Case |
|----------|-----------|----------|-------|----------|
| **TCP** | Connected, reliable | Guaranteed | Moderate | HTTP, databases, file transfer |
| **UDP** | Connectionless, unreliable | Not guaranteed | Fast | Gaming, video streaming, DNS |
| **WebSocket** | Persistent TCP + HTTP upgrade | Guaranteed | Moderate | Real-time web apps, chat |

## Exercises

1. Set up a Spring WebSocket server with STOMP and a simple chat endpoint.
2. Create a JavaScript client that connects and sends/receives messages.
3. Build a Netty-based WebSocket server for a real-time game.
4. Write a TCP echo server and a Java client that connects to it.
5. Compare latency between polling HTTP vs WebSocket for real-time updates.
