# Lesson 64: File Class

## Key Concepts
- The `java.io.File` class represents a file or directory path.
- `createNewFile()`: creates a new, empty file if it doesn't already exist (returns `boolean`).
- `getName()`, `getPath()`, `getAbsolutePath()`, `getParent()`: retrieve file path information.
- `exists()`, `isFile()`, `isDirectory()`: check file properties.
- `canRead()`, `canWrite()`: check file permissions.
- `length()`: returns file size in bytes.
- `delete()`: deletes the file or directory.
- `listFiles()`: returns an array of `File` objects in a directory.

## Code Example

```java
import java.io.File;

public class Main {
    public static void main(String[] args) {
        File file = new File("example.txt");

        try {
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("File already exists.");
            }

            System.out.println("\nFile info:");
            System.out.println("Name: " + file.getName());
            System.out.println("Path: " + file.getPath());
            System.out.println("Absolute path: " + file.getAbsolutePath());
            System.out.println("Parent: " + file.getParent());
            System.out.println("Exists: " + file.exists());
            System.out.println("Is file: " + file.isFile());
            System.out.println("Is directory: " + file.isDirectory());
            System.out.println("Can read: " + file.canRead());
            System.out.println("Can write: " + file.canWrite());
            System.out.println("Length: " + file.length() + " bytes");

            if (file.delete()) {
                System.out.println("\nFile deleted.");
            } else {
                System.out.println("\nFailed to delete file.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        File dir = new File(".");
        File[] files = dir.listFiles();
        if (files != null) {
            System.out.println("\nFiles in current directory:");
            for (File f : files) {
                if (f.isDirectory()) {
                    System.out.println("[DIR] " + f.getName());
                } else {
                    System.out.println("[FILE] " + f.getName());
                }
            }
        }
    }
}
```

## Explanation
1. A `File` object is created with the path `"example.txt"` (does not create the actual file yet).
2. `createNewFile()` creates the file on disk. Returns `true` if created, `false` if it already exists.
3. Various getter methods display file metadata (name, path, size, permissions, etc.).
4. `delete()` removes the file from disk.
5. A second `File` object pointing to `"."` (current directory) lists all files and subdirectories using `listFiles()`.
6. The enhanced for loop distinguishes directories from regular files with `isDirectory()`.

## Expected Output

```
File created: example.txt

File info:
Name: example.txt
Path: example.txt
Absolute path: /home/user/project/example.txt
Parent: null
Exists: true
Is file: true
Is directory: false
Can read: true
Can write: true
Length: 0 bytes

File deleted.

Files in current directory:
[FILE] example.txt
[FILE] Main.java
[FILE] Main.class
```
