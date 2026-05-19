# Lesson 82: JSlider

## Key Concepts
- `JSlider` for selecting a value from a range
- Setting tick marks with `setMajorTickSpacing()` and `setMinorTickSpacing()`
- Displaying ticks and labels with `setPaintTicks()` and `setPaintLabels()`
- `ChangeListener` with `addChangeListener()` to respond to value changes
- Dynamically updating a `JLabel` and `JProgressBar` based on slider position

## Code Example

```java
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JSlider Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Temperature: 25°C");
        label.setFont(new Font("Arial", Font.BOLD, 24));
        frame.add(label);

        JSlider slider = new JSlider(0, 100, 25);
        slider.setFont(new Font("Arial", Font.PLAIN, 14));
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            label.setText("Temperature: " + slider.getValue() + "°C");
        });
        frame.add(slider);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(25);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.BOLD, 14));
        progressBar.setPreferredSize(new Dimension(400, 30));
        slider.addChangeListener(e -> {
            progressBar.setValue(slider.getValue());
        });
        frame.add(progressBar);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JSlider` is created with a range of 0–100 and an initial value of 25.
2. `setMajorTickSpacing(10)` places a numbered label every 10 units; `setMinorTickSpacing(5)` places small ticks between them.
3. `setPaintTicks(true)` and `setPaintLabels(true)` make the ticks and numbers visible.
4. A `ChangeListener` updates the temperature label whenever the slider is moved.
5. A second `ChangeListener` synchronizes a `JProgressBar` to mirror the slider's value in real-time.

## Expected Output
- A window titled "JSlider Example" with a temperature label showing "Temperature: 25°C".
- A horizontal slider with tick marks and numerical labels at 0, 10, 20, ..., 100.
- Dragging the slider updates the label (e.g., "Temperature: 72°C") and the progress bar fills proportionally.
