# Lesson 77: JOptionPane

## Key Concepts
- `JOptionPane` for input dialogs, message dialogs, confirm dialogs, and option dialogs
- `showInputDialog()` — prompts the user for text input
- `showMessageDialog()` — displays a message to the user
- `showConfirmDialog()` — asks a yes/no/cancel question
- `showOptionDialog()` — displays custom button options
- Dialog types: `INFORMATION_MESSAGE`, `PLAIN_MESSAGE`, `WARNING_MESSAGE`, `QUESTION_MESSAGE`

## Code Example

```java
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog(null, "Enter your name:");
        if (name != null && !name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Hello " + name + "!", "Greeting", JOptionPane.INFORMATION_MESSAGE);
        }

        int age = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter your age:"));
        JOptionPane.showMessageDialog(null, "You are " + age + " years old.", "Age", JOptionPane.PLAIN_MESSAGE);

        int choice = JOptionPane.showConfirmDialog(null, "Do you like Java?", "Question", JOptionPane.YES_NO_CANCEL_OPTION);

        String message = switch (choice) {
            case JOptionPane.YES_OPTION -> "You like Java!";
            case JOptionPane.NO_OPTION -> "You don't like Java?";
            case JOptionPane.CANCEL_OPTION -> "You cancelled.";
            default -> "No selection.";
        };
        JOptionPane.showMessageDialog(null, message, "Result", JOptionPane.WARNING_MESSAGE);

        String[] options = {"Option A", "Option B", "Option C"};
        int selected = JOptionPane.showOptionDialog(null, "Choose one:", "Options",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        JOptionPane.showMessageDialog(null, "You selected: " + options[selected]);
    }
}
```

## Explanation
1. **`showInputDialog`** — displays a text field for the user to type input; returns the entered `String`.
2. **`showMessageDialog`** — shows a simple popup with a message and an OK button. The second parameter is the title bar text, the third is the message type icon.
3. **`showConfirmDialog`** — shows Yes/No/Cancel buttons and returns an `int` constant (`YES_OPTION`, `NO_OPTION`, `CANCEL_OPTION`).
4. **`showOptionDialog`** — displays a dialog with custom button labels passed as an array; returns the index of the clicked button.

## Expected Output
Sequential dialog boxes appear:
1. An input dialog asking for your name.
2. A message dialog greeting you.
3. An input dialog asking for your age.
4. A message dialog showing your age.
5. A confirm dialog asking "Do you like Java?" with Yes/No/Cancel buttons.
6. A warning dialog showing the result of your choice.
7. An option dialog offering "Option A", "Option B", "Option C".
8. A message dialog showing which option you selected.
