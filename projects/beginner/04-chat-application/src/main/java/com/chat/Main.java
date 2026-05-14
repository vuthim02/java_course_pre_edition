package com.chat;

import com.chat.client.ChatClient;
import com.chat.server.ChatServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "server" -> ChatServer.main(new String[0]);
                case "client" -> ChatClient.main(new String[0]);
                default -> printUsage();
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.print("Start as (server/client): ");
                String mode = reader.readLine();
                if (mode == null) return;
                switch (mode.toLowerCase().trim()) {
                    case "server" -> ChatServer.main(new String[0]);
                    case "client" -> ChatClient.main(new String[0]);
                    default -> printUsage();
                }
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java com.chat.Main [server|client]");
    }
}
