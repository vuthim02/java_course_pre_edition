# Lesson 65: FileWriter

## Key Concepts
- `FileWriter` writes character data to a file.
- `write(String)`: writes a string to the file.
- `append(CharSequence)`: appends text to the file.
- `String.format()` can be used to write formatted text.
- `close()`: closes the writer and flushes remaining data.
- The `FileWriter` constructor with a second `boolean` argument (`true`) enables **append mode** — data is added to the end of the file instead of overwriting it.
- `IOException` is a checked exception — must be handled with try-catch or declared.

## Code Example

```java
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            FileWriter writer = new FileWriter("output.txt");

            writer.write("Hello, World!\n");
            writer.write("This is a file write example.\n");
            writer.write("Java makes file I/O easy!\n");

            writer.append('\n');
            writer.append("Appended text.\n");

            writer.write(String.format("Number: %d, String: %s%n", 42, "Java"));

            writer.close();

            System.out.println("File written successfully.");

            FileWriter appendWriter = new FileWriter("output.txt", true);
            appendWriter.write("\nThis is appended text.\n");
            appendWriter.close();
            System.out.println("Text appended successfully.");

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
```

## Explanation
1. A `FileWriter` is created pointing to `"output.txt"`. Without `true`, it overwrites any existing content.
2. Multiple `write()` calls write lines to the file. `\n` adds newlines.
3. `append()` adds additional text at the end of the current content.
4. `String.format()` writes formatted data (`%d` for integers, `%s` for strings, `%n` for newline).
5. `close()` is called to flush and release system resources.
6. A second `FileWriter` with `true` as the second argument opens in **append mode**, adding text without overwriting.
7. All file I/O is wrapped in a try-catch for `IOException`.

## Expected Output

```
File written successfully.
Text appended successfully.
```

**Contents of output.txt:**
```
Hello, World!
This is a file write example.
Java makes file I/O easy!

Appended text.
Number: 42, String: Java

This is appended text.
```
