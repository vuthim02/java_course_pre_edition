# Lesson 95: Key Bindings

## Key Concepts
- Key Bindings map `KeyStroke` inputs to `Action` responses without focus issues
- `InputMap` associates a `KeyStroke` with a string key
- `ActionMap` associates a string key with an `Action`
- No need to manage focus or register listeners — Swing handles it
- `WHEN_IN_FOCUSED_WINDOW` makes bindings work even when the component doesn't have focus

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Key Bindings Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        GamePanel panel = new GamePanel();
        frame.add(panel);

        frame.setVisible(true);
    }
}

class GamePanel extends JPanel {
    private int x = 150;
    private int y = 100;
    private static final int MOVE = 10;

    GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);

        InputMap inputMap = getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "jump");

        actionMap.put("up", new MoveAction(0, -MOVE));
        actionMap.put("down", new MoveAction(0, MOVE));
        actionMap.put("left", new MoveAction(-MOVE, 0));
        actionMap.put("right", new MoveAction(MOVE, 0));
        actionMap.put("jump", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Jump! (x=" + x + ", y=" + y + ")");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GREEN);
        g.fillRect(x, y, 50, 50);
    }

    private class MoveAction extends AbstractAction {
        private int dx, dy;

        MoveAction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            x += dx;
            y += dy;
            x = Math.max(0, Math.min(getWidth() - 50, x));
            y = Math.max(0, Math.min(getHeight() - 50, y));
            repaint();
        }
    }
}
```

## Explanation
1. `InputMap` maps keystrokes (e.g., UP arrow) to logical names ("up").
2. `ActionMap` maps those names to `Action` objects that execute code.
3. `KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)` creates a keystroke for the UP arrow key with no modifiers.
4. `WHEN_IN_FOCUSED_WINDOW` means the binding works even if another component inside the window has focus.
5. `MoveAction` extends `AbstractAction` and updates `x`/`y` coordinates, clamping them so the square stays within the panel bounds, then calls `repaint()`.
6. The Jump action is defined inline as an anonymous `AbstractAction`.

## Expected Output

A 400x300 window with a black background and a green 50x50 square. Pressing arrow keys moves the square 10px in that direction. Pressing Space prints "Jump!" with the current position to the console.
