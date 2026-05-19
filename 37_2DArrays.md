# Lesson 37: 2D Arrays

## Key Concepts
- Declaring 2D arrays: `int[][] grid = new int[3][4];`
- Nested `for` loops to iterate over rows and columns
- `grid.length` gives the number of rows; `grid[row].length` gives columns per row
- Array literals for 2D arrays using nested curly braces `{{}, {}}`
- `printf()` with format specifiers (`%4d` for right-aligned integers, `%-10s` for left-aligned strings)
- Tic-Tac-Toe board simulation using a 2D `char` array

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        int[][] grid = new int[3][4];

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = (row + 1) * (col + 1);
            }
        }

        System.out.println("Multiplication table (3x4):");
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                System.out.printf("%4d", grid[row][col]);
            }
            System.out.println();
        }

        String[][] fruits = {
            {"apple", "banana", "cherry"},
            {"grape", "kiwi", "mango"},
            {"orange", "pear", "plum"}
        };

        System.out.println("\nFruits grid:");
        for (String[] row : fruits) {
            for (String fruit : row) {
                System.out.printf("%-10s", fruit);
            }
            System.out.println();
        }

        char[][] board = new char[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
        board[0][0] = 'X';
        board[1][1] = 'O';
        board[2][2] = 'X';

        System.out.println("\nTic-Tac-Toe board:");
        for (char[] row : board) {
            for (char cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }
}
```

## Explanation
1. `int[][] grid = new int[3][4]` creates a 3-row by 4-column grid filled with zeros. The nested loops fill it with multiplication table values.
2. `grid.length` is 3 (rows). `grid[row].length` is 4 (columns for that row). This works even if rows have different lengths (jagged arrays).
3. `String[][] fruits` is initialized with an array literal — each inner `{}` is a row.
4. For-each loops can be nested too: the outer loop gets each row as a `String[]`, the inner loop gets each element.
5. The `char[][] board` is initialized to all `'-'`, then specific positions are set to `'X'` and `'O'` to simulate a Tic-Tac-Toe game state.

## Expected Output

```
Multiplication table (3x4):
   1   2   3   4
   2   4   6   8
   3   6   9  12

Fruits grid:
apple     banana    cherry
grape     kiwi      mango
orange    pear      plum

Tic-Tac-Toe board:
X - -
- O -
- - X
```
