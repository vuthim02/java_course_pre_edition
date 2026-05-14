package com.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String prompt = in.readLine();
            System.out.println(prompt);

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String username = console.readLine();
            out.println(username);

            Thread receiver = new Thread(this::receiveMessages);
            receiver.setDaemon(true);
            receiver.start();

            String line;
            while (running && (line = console.readLine()) != null) {
                if (line.equals("/quit")) {
                    out.println(line);
                    break;
                }
                out.println(line);
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void receiveMessages() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[CLIENT] Connection lost.");
            }
        }
    }

    private void cleanup() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        new ChatClient("localhost", 12345).start();
    }
}
