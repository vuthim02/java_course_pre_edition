# Lesson 76: New Window

## Key Concepts
- Opening a new `JFrame` window from a button click
- Using lambda expressions for event handling
- Setting `JFrame.DISPOSE_ON_CLOSE` for secondary windows
- Separating window logic into a separate class

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Main Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("This is the main window.");
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        frame.add(label);

        JButton button = new JButton("Open New Window");
        button.setFont(new Font("Arial", Font.BOLD, 14));

        LaunchPage launchPage = new LaunchPage();
        button.addActionListener(e -> launchPage.openNewWindow());

        frame.add(button);
        frame.setVisible(true);
    }
}
```

The `LaunchPage` class handles creating the new window:

```java
class LaunchPage {
    void openNewWindow() {
        JFrame newWindow = new JFrame();
        newWindow.setTitle("New Window");
        newWindow.setSize(300, 200);
        newWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newWindow.setLayout(new FlowLayout());

        JLabel label = new JLabel("You opened a new window!");
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        newWindow.add(label);

        newWindow.setVisible(true);
    }
}
```

## Explanation
1. The **main window** is created as a standard `JFrame` with a label and a button.
2. When the button is clicked, a lambda expression calls `launchPage.openNewWindow()`.
3. The `LaunchPage` class creates a **new** `JFrame` instance, configures its properties, and makes it visible.
4. `DISPOSE_ON_CLOSE` is used on the new window so closing it does not exit the entire application — only the main window with `EXIT_ON_CLOSE` will terminate the program.

## Expected Output
- A main window titled "Main Window" with the text "This is the main window." and a button labeled "Open New Window".
- Clicking the button opens a second window titled "New Window" displaying "You opened a new window!".
- Closing the new window disposes it; closing the main window exits the application.
