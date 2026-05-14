# Chat Application

A multi-threaded console-based chat application demonstrating networking, threading, and I/O in Java.

## Features

- **Multi-threaded server** — accepts multiple clients concurrently, broadcasts messages
- **Console-based client** — send/receive messages in separate threads (non-blocking input)
- **Chat rooms** — create, join, and leave rooms dynamically
- **Username support** — each client chooses a name on connect
- **Message history** — new joiners see recent messages in the room

## How to run

### Start the server

```bash
javac ChatServer.java
java ChatServer
```

### Start one or more clients

```bash
javac ChatClient.java
java ChatClient
```

## Client commands (type inside the client)

| Command            | Description                     |
|--------------------|---------------------------------|
| `/join <room>`     | Join (or create) a chat room    |
| `/leave`           | Leave the current room          |
| `/rooms`           | List active rooms               |
| `/users`           | List users in the current room  |
| `/quit`            | Disconnect from server          |
| *any other text*   | Send a message to the room      |

## Architecture

```
Client 1 ──┐
Client 2 ──┼── Server (port 12345) ── broadcasts to room
Client 3 ──┘          │
                      ├── Room "general"
                      ├── Room "random"
                      └── ...
```
