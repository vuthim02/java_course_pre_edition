# Lesson 85: JFileChooser

## Key Concepts
- `JFileChooser` for open and save file dialogs
- `showOpenDialog()` — displays the standard "Open File" dialog
- `showSaveDialog()` — displays the "Save File" dialog
- `getSelectedFile()` — retrieves the chosen `File` object
- `File.getName()` — gets the file name as a string
- `JFileChooser.APPROVE_OPTION` — constant indicating the user confirmed the selection

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JFileChooser Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("No file selected");
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(label);

        JButton openButton = new JButton("Open File");
        openButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                label.setText("Selected: " + selectedFile.getName());
            }
        });
        frame.add(openButton);

        JButton saveButton = new JButton("Save File");
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                label.setText("Save as: " + selectedFile.getName());
            }
        });
        frame.add(saveButton);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JFileChooser` is created. Calling `showOpenDialog(frame)` opens the OS-native file picker for opening files.
2. The method returns an `int` — if the user clicks "Open" or "Save", the return value is `JFileChooser.APPROVE_OPTION`.
3. `getSelectedFile()` returns a `java.io.File` object representing the chosen file.
4. `selectedFile.getName()` extracts just the filename (e.g., "document.txt") for display.
5. The same pattern is used for `showSaveDialog()` to simulate a save operation.

## Expected Output
- A window titled "JFileChooser Example" with "No file selected" label and two buttons: "Open File" and "Save File".
- Clicking **Open File** opens a system file chooser dialog. After selecting a file, the label updates to "Selected: [filename]".
- Clicking **Save File** opens a save dialog. After choosing a location/name, the label updates to "Save as: [filename]".
