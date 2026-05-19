# Lesson 83: JProgressBar

## Key Concepts
- `JProgressBar` for displaying task progress
- `setStringPainted(true)` to show a numeric percentage
- Simulating a long-running task in a background thread
- `SwingUtilities.invokeLater()` to safely update GUI from a different thread
- Thread lifecycle with `new Thread(() -> { ... }).start()`

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JProgressBar Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Downloading...");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        frame.add(label);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.BOLD, 16));
        progressBar.setPreferredSize(new Dimension(350, 40));
        frame.add(progressBar);

        JButton button = new JButton("Start Download");
        button.addActionListener(e -> {
            new Thread(() -> {
                for (int i = 0; i <= 100; i++) {
                    int value = i;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(value);
                        label.setText("Downloading... " + value + "%");
                    });
                }
                SwingUtilities.invokeLater(() -> {
                    label.setText("Download Complete!");
                });
            }).start();
        });
        frame.add(button);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JProgressBar` is configured with a range of 0–100 and `setStringPainted(true)` so it displays a percentage string.
2. When "Start Download" is clicked, a **new thread** is spawned to simulate work.
3. Inside the thread, a loop iterates from 0 to 100, pausing 50ms each iteration with `Thread.sleep(50)`.
4. GUI updates are wrapped in `SwingUtilities.invokeLater()` because Swing components must be modified on the **Event Dispatch Thread (EDT)**, not from a background thread.
5. After the loop completes, the label text changes to "Download Complete!".

## Expected Output
- A window titled "JProgressBar Example" with a label "Downloading...", a progress bar at 0%, and a "Start Download" button.
- Clicking the button animates the progress bar from 0% to 100% over about 5 seconds.
- The label updates to show "Downloading... 45%", etc., and finally "Download Complete!".
