# Lesson 87: JTable

## Key Concepts
- `JTable` for displaying tabular data in rows and columns
- Using a 2D `Object[][]` array for data and a `String[]` array for column headers
- `JScrollPane` to make the table scrollable
- `setRowHeight()` to adjust row height
- `DefaultTableModel` for dynamic table management

## Code Example

```java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JTable Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        String[] columns = {"Name", "Age", "Grade"};
        Object[][] data = {
            {"Alice", 20, 'A'},
            {"Bob", 22, 'B'},
            {"Charlie", 21, 'A'},
            {"Diana", 23, 'C'},
            {"Eve", 20, 'B'}
        };

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);

        frame.setVisible(true);
    }
}
```

## Explanation
1. Column names are defined in a `String[]` array: `{"Name", "Age", "Grade"}`.
2. Row data is stored in a 2D `Object[][]` array — the first dimension is rows, the second is columns.
3. The `JTable` constructor accepts the data and column names and builds the grid automatically.
4. `table.setRowHeight(30)` increases the row height for better readability.
5. The table is wrapped in a `JScrollPane` so that if there are many rows, the user can scroll.
6. `DefaultTableModel` (imported but not used in this simple example) allows adding/removing rows dynamically at runtime.

## Expected Output
- A window titled "JTable Example" containing a scrollable table with 5 rows and 3 columns.
- The table displays:
  - Alice | 20 | A
  - Bob   | 22 | B
  - Charlie | 21 | A
  - Diana | 23 | C
  - Eve   | 20 | B
- The "Grade" column shows character values ('A', 'B', 'C').
