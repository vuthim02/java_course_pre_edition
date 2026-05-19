# Lesson 66: FileReader

## Key Concepts
- `FileReader` reads character data from a file.
- `BufferedReader` wraps a `FileReader` for efficient line-by-line reading via `readLine()`.
- `readLine()` returns the next line as a `String`, or `null` when the end of the file is reached.
- `read()` reads a single character, returning its int value (or `-1` at end of file).
- **Try-with-resources** (`try (Resource r = new Resource(...))`) automatically closes the resource.
- Both `FileReader` and `BufferedReader` throw `IOException`.

## Code Example

```java
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            FileReader reader = new FileReader("output.txt");
            BufferedReader bufferedReader = new BufferedReader(reader);

            System.out.println("=== File Contents ===");
            String line;
            int lineNumber = 1;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(lineNumber + ": " + line);
                lineNumber++;
            }

            bufferedReader.close();
            System.out.println("\nTotal lines: " + (lineNumber - 1));

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        try (FileReader reader = new FileReader("output.txt")) {
            System.out.println("\n=== Reading character by character ===");
            int data;
            while ((data = reader.read()) != -1) {
                System.out.print((char) data);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

## Explanation
1. **Line-by-line reading**: A `FileReader` is wrapped in a `BufferedReader`. The `while` loop calls `readLine()` until it returns `null`. Each line is printed with its line number.
2. `close()` is called to release resources.
3. **Character-by-character reading**: A try-with-resources block opens a `FileReader`. `read()` returns an int for each character (or `-1` at EOF). Casting to `char` prints the actual character.
4. Try-with-resources automatically closes the `FileReader` when the block exits.
5. Both approaches handle `IOException` with catch blocks.

## Expected Output

```
=== File Contents ===
1: Hello, World!
2: This is a file write example.
3: Java makes file I/O easy!
4: 
5: Appended text.
6: Number: 42, String: Java

Total lines: 6

=== Reading character by character ===
Hello, World!
This is a file write example.
Java makes file I/O easy!

Appended text.
Number: 42, String: Java
```
