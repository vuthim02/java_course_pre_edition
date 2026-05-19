# Lesson 103: Packaging into a JAR File

## Key Concepts
- A JAR (Java ARchive) bundles compiled `.class` files into a single distributable file
- An executable JAR can be run with `java -jar filename.jar`
- A manifest file (`MANIFEST.MF`) specifies the main class entry point
- JAR files use ZIP compression internally
- Modern IDEs (IntelliJ, Eclipse) provide GUI tools for JAR creation

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Creating Executable JAR ===");
        System.out.println("Steps to create a JAR file:");
        System.out.println();
        System.out.println("1. Compile:  javac Main.java");
        System.out.println("2. Manifest: echo Main-Class: Main > MANIFEST.MF");
        System.out.println("3. Package:  jar cfm MyProgram.jar MANIFEST.MF *.class");
        System.out.println("4. Run:      java -jar MyProgram.jar");
        System.out.println();
        System.out.println("Or with a single command:");
        System.out.println("  jar cfe MyProgram.jar Main Main.class");
        System.out.println();
        System.out.println("For IntelliJ:");
        System.out.println("  File -> Project Structure -> Artifacts -> + -> JAR -> From modules");
        System.out.println("  Build -> Build Artifacts -> Build");
        System.out.println();
        System.out.println("For Eclipse:");
        System.out.println("  File -> Export -> Java -> Runnable JAR File");
        System.out.println();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My JAR Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new FlowLayout());

            JLabel label = new JLabel("This runs from a JAR file!");
            label.setFont(new Font("Arial", Font.BOLD, 20));
            frame.add(label);

            frame.setVisible(true);
        });
    }
}
```

## Explanation
1. **Compile** — `javac Main.java` produces `Main.class`.
2. **Manifest** — `MANIFEST.MF` is a text file with `Main-Class: Main` (followed by a blank line). This tells Java which class has the `main()` method.
3. **Package** — `jar cfm MyProgram.jar MANIFEST.MF *.class`
   - `c` = create new archive
   - `f` = specify output filename
   - `m` = include manifest file
4. **Run** — `java -jar MyProgram.jar` executes the packaged application.
5. **Shortcut** — `jar cfe MyProgram.jar Main Main.class` combines steps 2 and 3 (the `e` flag sets the entry point).
6. `SwingUtilities.invokeLater()` ensures the GUI is created on the Event Dispatch Thread.
7. IDEs like IntelliJ and Eclipse automate the entire process through project settings.

## Expected Output

When run from the terminal after packaging:
```
=== Creating Executable JAR ===
Steps to create a JAR file:

1. Compile:  javac Main.java
2. Manifest: echo Main-Class: Main > MANIFEST.MF
3. Package:  jar cfm MyProgram.jar MANIFEST.MF *.class
4. Run:      java -jar MyProgram.jar

Or with a single command:
  jar cfe MyProgram.jar Main Main.class

For IntelliJ:
  File -> Project Structure -> Artifacts -> + -> JAR -> From modules
  Build -> Build Artifacts -> Build

For Eclipse:
  File -> Export -> Java -> Runnable JAR File
```

A GUI window also opens titled "My JAR Application" displaying the message "This runs from a JAR file!" in bold 20pt Arial font.
