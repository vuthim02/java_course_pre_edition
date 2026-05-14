# Core Java Advanced — Lesson 2: File I/O & NIO

## Java I/O Overview

Java has two I/O systems: **Classic I/O** (java.io) and **NIO** (java.nio — New I/O).

```
Classic I/O (java.io):         NIO (java.nio):
• Stream-based (byte by byte)  • Buffer-based (chunks)
• Blocking operations          • Non-blocking possible
• Simpler API                  • More powerful, faster
• Good for small files         • Good for large files / servers
```

## Classic I/O — Reading Files

```java
// Character-based (text files)
// FileReader + BufferedReader
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
} catch (IOException e) {
    System.err.println("Error reading file: " + e.getMessage());
}

// Byte-based (binary files)
// FileInputStream + BufferedInputStream
try (FileInputStream fis = new FileInputStream("image.jpg");
     BufferedInputStream bis = new BufferedInputStream(fis)) {
    byte[] buffer = new byte[4096];
    int bytesRead;
    while ((bytesRead = bis.read(buffer)) != -1) {
        // Process bytes 0..bytesRead-1
    }
}
```

## Classic I/O — Writing Files

```java
// Character-based
try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
    writer.write("Hello, World!");
    writer.newLine();
    writer.write("Second line");
} catch (IOException e) {
    System.err.println("Error writing: " + e.getMessage());
}

// Appending
try (BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true))) {
    writer.write("New log entry");
    writer.newLine();
}
```

## NIO.2 (Java 7+) — The Modern Way

### The Path API

```java
// Creating paths
Path path1 = Path.of("/home/user/file.txt");           // Java 11+
Path path2 = Paths.get("/home", "user", "file.txt");   // Pre-Java 11
Path path3 = Path.of(".", "data", "config.properties"); // Relative

// Path operations
System.out.println(path1.getFileName());    // "file.txt"
System.out.println(path1.getParent());      // "/home/user"
System.out.println(path1.getRoot());        // "/"
System.out.println(path1.toAbsolutePath());  // Full path

// Resolve
Path configDir = Path.of("/etc", "myapp");
Path configFile = configDir.resolve("config.properties");  // /etc/myapp/config.properties
```

### Reading and Writing with NIO

```java
// Read entire file into String (Java 11+)
String content = Files.readString(Path.of("file.txt"));

// Read all lines into List<String>
List<String> lines = Files.readAllLines(Path.of("file.txt"));

// Write String to file (Java 11+)
Files.writeString(Path.of("output.txt"), "Hello, NIO!");

// Write lines
Files.write(Path.of("output.txt"), List.of("Line 1", "Line 2"));

// Append
Files.writeString(Path.of("log.txt"), "New entry\n", StandardOpenOption.APPEND);
```

### Working with Files and Directories

```java
Path path = Path.of("test.txt");

// Check
boolean exists = Files.exists(path);
boolean isDir = Files.isDirectory(path);
boolean isFile = Files.isRegularFile(path);
boolean isHidden = Files.isHidden(path);

// Size
long bytes = Files.size(path);

// Operations
Files.createFile(Path.of("new.txt"));
Files.createDirectory(Path.of("newdir"));
Files.createDirectories(Path.of("a/b/c/d"));  // Creates all missing parent dirs
Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
Files.move(source, target);
Files.delete(path);               // Throws if doesn't exist
Files.deleteIfExists(path);       // Returns boolean

// Temporary files/directories
Path tmpFile = Files.createTempFile("prefix", ".tmp");
Path tmpDir = Files.createTempDirectory("mytemp");
```

### Walking the File Tree

```java
// List files in directory
try (Stream<Path> stream = Files.list(Path.of("."))) {
    stream.forEach(System.out::println);
}

// Walk recursively (depth-first)
try (Stream<Path> stream = Files.walk(Path.of("src"), 3)) {
    stream.filter(Files::isRegularFile)
          .forEach(System.out::println);
}

// Find files matching a predicate
try (Stream<Path> stream = Files.find(
        Path.of("src"),
        Integer.MAX_VALUE,
        (path, attrs) -> path.toString().endsWith(".java"))) {
    stream.forEach(System.out::println);
}
```

### File Attributes

```java
Path file = Path.of("test.txt");

// Basic attributes
BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
System.out.println("Size: " + attrs.size());
System.out.println("Created: " + attrs.creationTime());
System.out.println("Modified: " + attrs.lastModifiedTime());
System.out.println("Is directory: " + attrs.isDirectory());
System.out.println("Is regular file: " + attrs.isRegularFile());
```

### File Watching (WatchService)

```java
try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
    Path dir = Path.of(".");
    dir.register(watcher,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.ENTRY_DELETE);

    System.out.println("Watching directory for changes...");

    while (true) {
        WatchKey key = watcher.take();  // Blocks until event
        for (WatchEvent<?> event : key.pollEvents()) {
            System.out.println(event.kind() + ": " + event.context());
        }
        key.reset();  // Important!
    }
}
```

## Console Input

```java
// Scanner (most common for beginners)
Scanner scanner = new Scanner(System.in);
System.out.print("Enter your name: ");
String name = scanner.nextLine();
System.out.print("Enter your age: ");
int age = scanner.nextInt();
System.out.println("Hello, " + name + "! You are " + age + " years old.");

// Console class (more secure for passwords)
Console console = System.console();
if (console != null) {
    String username = console.readLine("Username: ");
    char[] password = console.readPassword("Password: ");  // Not a String!
    java.util.Arrays.fill(password, ' ');  // Clear the password
}
```

---

### Exercises

1. Write a program that reads a text file, counts the number of words, and writes the count to another file.
2. Write a file copy utility that copies files using NIO.
3. Write a program that walks a directory tree and prints all `.java` files with their sizes.
4. Create a program that watches a directory and prints a message when new files are added.
5. Implement a simple log file reader that reads a growing log file (like `tail -f`).
