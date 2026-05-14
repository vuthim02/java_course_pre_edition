# File Manager — Complete Java Source Code

File: `FileManager.java`

```java
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * A console-based file manager built on Java NIO.
 * Supports listing, navigating, copying, moving, deleting, searching,
 * previewing, and calculating sizes of files and directories.
 */
public class FileManager {

    private static Path currentDir = Paths.get("").toAbsolutePath().normalize();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("========== FILE MANAGER ==========");
        System.out.println("Commands: ls, cd, cp, mv, rm, search, preview, size, pwd, exit");

        while (true) {
            // Show prompt with current directory
            System.out.print("\n" + currentDir + "\n> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            // Split into command and argument(s)
            String[] parts = input.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1].trim() : "";

            try {
                switch (cmd) {
                    case "exit"      -> { System.out.println("Goodbye!");
                                          scanner.close(); return; }
                    case "ls"        -> listFiles(arg);
                    case "cd"        -> changeDirectory(arg);
                    case "pwd"       -> System.out.println(currentDir);
                    case "cp"        -> copy(arg);
                    case "mv"        -> move(arg);
                    case "rm"        -> delete(arg);
                    case "search"    -> search(arg);
                    case "preview"   -> preview(arg);
                    case "size"      -> calculateSize(arg);
                    default -> System.out.println(
                            "Commands: ls, cd, cp, mv, rm, search, preview, size, pwd, exit");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------
    //  ls  — list directory contents with type, permissions, size, date
    // ---------------------------------------------------------------
    private static void listFiles(String arg) throws IOException {
        Path target = currentDir.resolve(arg.isEmpty() ? "." : arg).normalize();
        try (Stream<Path> stream = Files.list(target)) {
            stream.sorted().forEach(path -> {
                try {
                    BasicFileAttributes attrs =
                            Files.readAttributes(path, BasicFileAttributes.class);
                    char type      = attrs.isDirectory() ? 'd' : '-';
                    String perms   = getPermissions(path);
                    long size      = attrs.size();
                    String modTime = attrs.lastModifiedTime()
                            .toString().replace('T', ' ').substring(0, 19);
                    String name    = path.getFileName().toString();
                    System.out.printf("%c%s %8d %s %s%n", type, perms, size, modTime, name);
                } catch (IOException ignored) {
                    // skip files we can't read
                }
            });
        }
    }

    /** Returns a 3-char string like "rwx" for the current user */
    private static String getPermissions(Path path) {
        StringBuilder sb = new StringBuilder(3);
        sb.append(Files.isReadable(path)  ? "r" : "-");
        sb.append(Files.isWritable(path)  ? "w" : "-");
        sb.append(Files.isExecutable(path) ? "x" : "-");
        return sb.toString();
    }

    // ---------------------------------------------------------------
    //  cd  — change current directory
    // ---------------------------------------------------------------
    private static void changeDirectory(String arg) {
        if (arg.isEmpty()) {
            // Reset to user's working directory
            currentDir = Paths.get("").toAbsolutePath().normalize();
            return;
        }
        Path newDir = currentDir.resolve(arg).normalize();
        if (Files.isDirectory(newDir)) {
            currentDir = newDir;
        } else {
            System.out.println("Not a directory: " + newDir);
        }
    }

    // ---------------------------------------------------------------
    //  cp  — copy file or directory (recursive)
    // ---------------------------------------------------------------
    private static void copy(String arg) throws IOException {
        String[] parts = arg.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: cp <source> <dest>");
            return;
        }
        Path src = currentDir.resolve(parts[0]).normalize();
        Path dst = currentDir.resolve(parts[1]).normalize();

        if (Files.isDirectory(src)) {
            // Walk the source tree and copy each entry
            try (Stream<Path> walk = Files.walk(src)) {
                walk.forEach(source -> {
                    try {
                        Path dest = dst.resolve(src.relativize(source));
                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("  Error copying " + source + ": " + e.getMessage());
                    }
                });
            }
        } else {
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Copied: " + src + " -> " + dst);
    }

    // ---------------------------------------------------------------
    //  mv  — move / rename file or directory
    // ---------------------------------------------------------------
    private static void move(String arg) throws IOException {
        String[] parts = arg.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: mv <source> <dest>");
            return;
        }
        Path src = currentDir.resolve(parts[0]).normalize();
        Path dst = currentDir.resolve(parts[1]).normalize();
        Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Moved: " + src + " -> " + dst);
    }

    // ---------------------------------------------------------------
    //  rm  — delete file or directory (recursive, depth-first)
    // ---------------------------------------------------------------
    private static void delete(String arg) throws IOException {
        if (arg.isEmpty()) { System.out.println("Usage: rm <path>"); return; }
        Path target = currentDir.resolve(arg).normalize();
        if (Files.isDirectory(target)) {
            // Delete children first (reverse order), then the directory itself
            try (Stream<Path> walk = Files.walk(target)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); }
                        catch (IOException ignored) { }
                    });
            }
        } else {
            Files.deleteIfExists(target);
        }
        System.out.println("Deleted: " + target);
    }

    // ---------------------------------------------------------------
    //  search  — find files by name substring or glob pattern
    // ---------------------------------------------------------------
    private static void search(String arg) throws IOException {
        if (arg.isEmpty()) { System.out.println("Usage: search <name or glob>"); return; }
        boolean isGlob = arg.contains("*") || arg.contains("?");

        try (Stream<Path> stream = Files.walk(currentDir)) {
            stream.filter(p -> {
                String name = p.getFileName().toString();
                if (isGlob) {
                    PathMatcher matcher = FileSystems.getDefault()
                            .getPathMatcher("glob:" + arg);
                    return matcher.matches(p.getFileName());
                }
                return name.toLowerCase().contains(arg.toLowerCase());
            }).forEach(p -> {
                try {
                    System.out.printf("%s (%d bytes)%n", p, Files.size(p));
                } catch (IOException e) {
                    System.out.println(p);
                }
            });
        }
    }

    // ---------------------------------------------------------------
    //  preview  — show first 10 lines of a text file
    // ---------------------------------------------------------------
    private static void preview(String arg) throws IOException {
        if (arg.isEmpty()) { System.out.println("Usage: preview <file>"); return; }
        Path file = currentDir.resolve(arg).normalize();
        if (!Files.isRegularFile(file)) {
            System.out.println("Not a regular file.");
            return;
        }

        // Check content type; if unknown but extension looks textual, try anyway
        String contentType = Files.probeContentType(file);
        boolean isText = (contentType != null && contentType.startsWith("text"))
                || arg.endsWith(".txt") || arg.endsWith(".java")
                || arg.endsWith(".md")  || arg.endsWith(".xml")
                || arg.endsWith(".json") || arg.endsWith(".properties")
                || arg.endsWith(".csv");

        if (!isText) {
            System.out.println("Binary file. Size: " + Files.size(file) + " bytes");
            return;
        }

        // Read all lines into a list for preview and line count
        List<String> lines = Files.readAllLines(file);
        int total = lines.size();
        int show  = Math.min(total, 10);

        System.out.println("--- Preview: " + file.getFileName() + " ---");
        for (int i = 0; i < show; i++) {
            System.out.println(lines.get(i));
        }
        if (total > 10) {
            System.out.println("... (" + (total - 10) + " more lines)");
        }
    }

    // ---------------------------------------------------------------
    //  size  — calculate recursive size of a file or directory
    // ---------------------------------------------------------------
    private static void calculateSize(String arg) throws IOException {
        Path target = currentDir.resolve(arg.isEmpty() ? "." : arg).normalize();
        long size;
        if (Files.isDirectory(target)) {
            // Walk the tree, sum sizes of regular files
            try (Stream<Path> walk = Files.walk(target)) {
                size = walk.filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try { return Files.size(p); }
                            catch (IOException e) { return 0L; }
                        })
                        .sum();
            }
        } else {
            size = Files.size(target);
        }
        System.out.println("Total size: " + formatSize(size));
    }

    /** Human-readable byte count */
    private static String formatSize(long bytes) {
        if (bytes < 1024)               return bytes + " B";
        if (bytes < 1024 * 1024)        return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
```
