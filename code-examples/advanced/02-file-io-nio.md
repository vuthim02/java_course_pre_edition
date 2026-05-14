# File I/O and NIO.2

This document covers Java file I/O using both the legacy `java.io` API and the modern NIO.2 (`java.nio.file`) API, including file attributes, directory traversal, and file change monitoring.

## Legacy File Class

```java
import java.io.*;

public class FileClassDemo {
    public static void main(String[] args) throws IOException {
        System.out.println("--- java.io.File Demo ---");

        File dir = new File("demo_dir/subdir");
        if (dir.mkdirs()) {
            System.out.println("Created directories: " + dir.getAbsolutePath());
        }

        File file = new File(dir, "test.txt");
        System.out.println("File exists: " + file.exists());

        if (file.createNewFile()) {
            System.out.println("Created file: " + file.getName());
        }
        System.out.println("Is directory: " + file.isDirectory());
        System.out.println("Is file: " + file.isFile());
        System.out.println("Length: " + file.length() + " bytes");
        System.out.println("Absolute path: " + file.getAbsolutePath());

        // List files in a directory
        File parentDir = new File("demo_dir");
        File[] files = parentDir.listFiles();
        if (files != null) {
            System.out.println("\nContents of demo_dir:");
            for (File f : files) {
                System.out.println("  " + (f.isDirectory() ? "[DIR] " : "[FILE] ") + f.getName());
            }
        }

        // Delete on exit (useful for temp files)
        file.deleteOnExit();
        dir.deleteOnExit();
        parentDir.deleteOnExit();
    }
}
```

## FileReader/FileWriter with Buffering

```java
import java.io.*;

public class ReaderWriterDemo {
    public static void main(String[] args) throws IOException {
        System.out.println("--- FileReader / FileWriter with Buffering ---");

        // Writing with PrintWriter (convenient auto-flush)
        File file = new File("text_demo.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Line 1: Hello, World!");
            writer.println("Line 2: Java I/O is powerful.");
            writer.printf("Line 3: Formatted %s %d%n", "value", 42);
            writer.flush();
        }
        System.out.println("Written " + file.length() + " bytes to " + file.getName());

        // Reading with BufferedReader (efficient line-by-line)
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println("Read: " + line);
                lineNum++;
            }
            System.out.println("Total lines: " + lineNum);
        }

        // Cleanup
        file.delete();
    }
}
```

## NIO.2: Path, Paths, and Files

```java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class NIO2BasicsDemo {
    public static void main(String[] args) throws IOException {
        System.out.println("--- NIO.2: Path, Paths, Files ---");

        // Creating paths
        Path dir = Paths.get("nio_demo");
        Path file = dir.resolve("example.txt");
        System.out.println("Path: " + file.toAbsolutePath());

        // Create directories and file
        Files.createDirectories(dir);
        List<String> lines = List.of(
            "Hello from NIO.2",
            "Reading all lines at once",
            "Using Files.readAllLines()"
        );
        Files.write(file, lines, StandardCharsets.UTF_8);
        System.out.println("Wrote " + lines.size() + " lines");

        // Read all lines at once
        List<String> readLines = Files.readAllLines(file, StandardCharsets.UTF_8);
        System.out.println("Read back:");
        readLines.forEach(l -> System.out.println("  " + l));

        // Copy and move
        Path copyTarget = dir.resolve("copy_of_example.txt");
        Files.copy(file, copyTarget, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Copied to: " + copyTarget.getFileName());

        Path movedTarget = dir.resolve("renamed_example.txt");
        Files.move(file, movedTarget, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Moved (renamed) to: " + movedTarget.getFileName());

        // Delete
        Files.delete(copyTarget);
        System.out.println("Deleted: " + copyTarget.getFileName());

        // Files.lines() — stream-based reading (lazy, auto-closed)
        System.out.println("\n--- Files.lines() stream API ---");
        try (var stream = Files.lines(movedTarget, StandardCharsets.UTF_8)) {
            stream.map(String::toUpperCase)
                  .forEach(l -> System.out.println("  " + l));
        }

        // Cleanup
        Files.delete(movedTarget);
        Files.delete(dir);
    }
}
```

## DirectoryStream and FileVisitor

```java
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryStreamDemo {
    public static void main(String[] args) throws IOException {
        // Create test structure
        Path testDir = Files.createTempDirectory("demo_");
        Path sub = Files.createDirectory(testDir.resolve("sub"));
        Files.writeString(testDir.resolve("a.txt"), "aaa");
        Files.writeString(testDir.resolve("b.txt"), "bbb");
        Files.writeString(sub.resolve("c.txt"), "ccc");

        System.out.println("--- DirectoryStream ---");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(testDir, "*.txt")) {
            for (Path entry : stream) {
                System.out.println("  " + entry.getFileName());
            }
        }

        System.out.println("\n--- FileVisitor (SimpleFileVisitor) ---");
        Files.walkFileTree(testDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                System.out.println("[DIR] " + dir.getFileName());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                System.out.println("  " + file.getFileName() + " (" + attrs.size() + " bytes)");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("Failed: " + file);
                return FileVisitResult.CONTINUE;
            }
        });

        // Cleanup
        try (var walk = Files.walk(testDir)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        }
    }
}
```

## File Attribute Views

```java
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;

public class FileAttributesDemo {
    public static void main(String[] args) throws IOException {
        Path tempFile = Files.createTempFile("attr_", ".txt");
        Files.writeString(tempFile, "Attribute demo");

        System.out.println("--- BasicFileAttributes ---");
        BasicFileAttributes attrs = Files.readAttributes(tempFile, BasicFileAttributes.class);
        System.out.println("Size: " + attrs.size() + " bytes");
        System.out.println("Is directory: " + attrs.isDirectory());
        System.out.println("Is regular file: " + attrs.isRegularFile());
        System.out.println("Is symbolic link: " + attrs.isSymbolicLink());
        System.out.println("Creation time: " + attrs.creationTime());
        System.out.println("Last modified: " + attrs.lastModifiedTime());
        System.out.println("Last accessed: " + attrs.lastAccessTime());

        // PosixFileAttributes (on POSIX systems)
        try {
            PosixFileAttributes posix = Files.readAttributes(tempFile, PosixFileAttributes.class);
            System.out.println("\n--- PosixFileAttributes ---");
            System.out.println("Owner: " + posix.owner().getName());
            System.out.println("Group: " + posix.group().getName());
            System.out.println("Permissions: " + posix.permissions());
        } catch (UnsupportedOperationException e) {
            System.out.println("\n(POSIX attributes not supported on this filesystem)");
        }

        // Set file permissions (POSIX)
        try {
            Files.setPosixFilePermissions(tempFile,
                Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            System.out.println("Set permissions to rw-------");
        } catch (UnsupportedOperationException e) {
            System.out.println("(Cannot set POSIX permissions here)");
        }

        Files.deleteIfExists(tempFile);
    }
}
```

## WatchService for File Change Monitoring

```java
import java.io.IOException;
import java.nio.file.*;

public class WatchServiceDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Creates a temp directory to watch
        Path dir = Files.createTempDirectory("watch_");
        System.out.println("Watching directory: " + dir);

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            // Register directory for events
            dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

            // In a real app, this runs in a dedicated thread
            System.out.println("Creating a file to trigger watch event...");
            Files.createFile(dir.resolve("new_file.txt"));

            WatchKey key = watcher.take(); // blocks until event
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path filename = (Path) event.context();
                System.out.println(kind.name() + ": " + filename);
            }
            key.reset();

            // Cleanup
            Files.delete(dir.resolve("new_file.txt"));
            Files.delete(dir);
        }
    }
}
```

## Reading and Writing Binary Files

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class BinaryFileDemo {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("binary_", ".bin");

        System.out.println("--- Binary I/O with Files.readAllBytes/write ---");

        // Write binary data
        byte[] dataToWrite = {0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x00, (byte) 0xFF};
        Files.write(file, dataToWrite);
        System.out.println("Written bytes: " + Arrays.toString(dataToWrite));

        // Read all bytes at once
        byte[] readData = Files.readAllBytes(file);
        System.out.println("Read bytes:    " + Arrays.toString(readData));
        System.out.println("Match: " + Arrays.equals(dataToWrite, readData));

        // Binary copy with InputStream/OutputStream
        Path copy = file.resolveSibling("copy.bin");
        try (var in = Files.newInputStream(file);
             var out = Files.newOutputStream(copy)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("Copied binary file: " + copy.getFileName());

        Files.deleteIfExists(file);
        Files.deleteIfExists(copy);
    }
}
```
