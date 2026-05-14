package com.filemanager;

import com.filemanager.service.FileService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class FileManager {

    private static final FileService fileService = new FileService();
    private static final Scanner scanner = new Scanner(System.in);
    private static Path currentDir = Path.of(System.getProperty("user.dir"));

    public static void main(String[] args) {
        System.out.println("========== FILE MANAGER ==========");
        System.out.println("Commands: list, navigate, copy, move, delete, search, tree, preview, info, exit");
        System.out.println("Current dir: " + currentDir.toAbsolutePath().normalize());

        while (true) {
            System.out.print("\n> ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            try {
                switch (cmd) {
                    case "list" -> list();
                    case "navigate" -> navigate();
                    case "copy" -> copy();
                    case "move" -> move();
                    case "delete" -> delete();
                    case "search" -> search();
                    case "tree" -> tree();
                    case "preview" -> preview();
                    case "info" -> info();
                    case "exit" -> { System.out.println("Goodbye!"); return; }
                    default -> System.out.println("Unknown command. Try: list, navigate, copy, move, delete, search, tree, preview, info, exit");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void list() {
        List<String> entries = fileService.listFiles(currentDir);
        if (entries.isEmpty()) {
            System.out.println("(empty directory)");
        } else {
            entries.forEach(System.out::println);
        }
    }

    private static void navigate() {
        System.out.print("Enter path (or '..' for parent): ");
        String input = scanner.nextLine().trim();
        Path target;
        if ("..".equals(input)) {
            target = currentDir.getParent();
            if (target == null) {
                System.out.println("Already at root directory");
                return;
            }
        } else {
            try {
                target = currentDir.resolve(input).normalize();
            } catch (InvalidPathException e) {
                System.out.println("Invalid path: " + e.getMessage());
                return;
            }
        }
        if (Files.isDirectory(target)) {
            currentDir = target;
            System.out.println("Current dir: " + currentDir.toAbsolutePath().normalize());
        } else {
            System.out.println("Not a directory: " + target);
        }
    }

    private static void copy() {
        try {
            System.out.print("Source path: ");
            Path source = resolvePath(scanner.nextLine().trim());
            System.out.print("Target path: ");
            Path target = resolvePath(scanner.nextLine().trim());
            fileService.copyFile(source, target);
            System.out.println("Copied: " + source + " -> " + target);
        } catch (IOException e) {
            System.out.println("Copy failed: " + e.getMessage());
        }
    }

    private static void move() {
        try {
            System.out.print("Source path: ");
            Path source = resolvePath(scanner.nextLine().trim());
            System.out.print("Target path: ");
            Path target = resolvePath(scanner.nextLine().trim());
            fileService.moveFile(source, target);
            System.out.println("Moved: " + source + " -> " + target);
        } catch (IOException e) {
            System.out.println("Move failed: " + e.getMessage());
        }
    }

    private static void delete() {
        try {
            System.out.print("Path to delete: ");
            Path path = resolvePath(scanner.nextLine().trim());
            System.out.print("Are you sure? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if ("yes".equals(confirm) || "y".equals(confirm)) {
                fileService.deleteFile(path);
                System.out.println("Deleted: " + path);
            } else {
                System.out.println("Deletion cancelled");
            }
        } catch (IOException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }

    private static void search() {
        try {
            System.out.print("Search pattern (glob, e.g. *.txt): ");
            String pattern = scanner.nextLine().trim();
            System.out.print("Root directory [current]: ");
            String rootInput = scanner.nextLine().trim();
            Path root = rootInput.isEmpty() ? currentDir : resolvePath(rootInput);
            List<String> results = fileService.searchFiles(root, pattern);
            if (results.isEmpty()) {
                System.out.println("No matches found");
            } else {
                System.out.println("Found " + results.size() + " match(es):");
                results.forEach(System.out::println);
            }
        } catch (IOException e) {
            System.out.println("Search failed: " + e.getMessage());
        }
    }

    private static void tree() {
        try {
            System.out.print("Max depth [3]: ");
            String depthInput = scanner.nextLine().trim();
            int depth = depthInput.isEmpty() ? 3 : Integer.parseInt(depthInput);
            System.out.print("Root path [current]: ");
            String rootInput = scanner.nextLine().trim();
            Path root = rootInput.isEmpty() ? currentDir : resolvePath(rootInput);
            fileService.printTree(root, depth).forEach(System.out::println);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private static void preview() {
        try {
            System.out.print("File path: ");
            Path path = resolvePath(scanner.nextLine().trim());
            String preview = fileService.previewFile(path);
            System.out.println(preview);
        } catch (IOException e) {
            System.out.println("Preview failed: " + e.getMessage());
        }
    }

    private static void info() {
        try {
            System.out.print("File path: ");
            Path path = resolvePath(scanner.nextLine().trim());
            String info = fileService.fileInfo(path);
            System.out.print(info);
        } catch (IOException e) {
            System.out.println("Info failed: " + e.getMessage());
        }
    }

    private static Path resolvePath(String input) {
        if (input.isEmpty()) return currentDir;
        try {
            Path p = Path.of(input);
            if (p.isAbsolute()) return p.normalize();
            return currentDir.resolve(input).normalize();
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path: " + e.getMessage());
        }
    }
}
