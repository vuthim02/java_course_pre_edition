# Lesson 27: Nested Loops

## Key Concepts
- A loop inside another loop
- Inner loop completes all iterations *for each* outer loop iteration
- Drawing shapes (rectangle, triangle) with nested loops
- Building a number pyramid
- Simulating a clock with nested loops

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter number of rows: ");
        int rows = scanner.nextInt();

        System.out.print("Enter number of columns: ");
        int cols = scanner.nextInt();

        System.out.println("\nRectangle:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }

        System.out.println("\nTriangle:");
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }

        System.out.println("\nNumber pyramid:");
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print(j + " ");
            }
            System.out.println();
        }

        System.out.println("\nClock (hours:minutes):");
        for (int hour = 0; hour < 3; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                System.out.printf("%02d:%02d  ", hour, minute);
            }
            System.out.println();
        }

        scanner.close();
    }
}
```

## Explanation
1. **Rectangle**: inner loop prints `cols` asterisks; outer loop repeats for `rows` lines.
2. **Triangle**: inner loop runs up to `i` (the current row number), so each row has one more star.
3. **Number pyramid**: same structure as the triangle, but prints the column number `j`.
4. **Clock**: outer loop iterates hours, inner loop iterates minutes in 15-minute intervals. `printf("%02d")` pads with a leading zero.

## Expected Output

```
Enter number of rows: 4
Enter number of columns: 6

Rectangle:
* * * * * * 
* * * * * * 
* * * * * * 
* * * * * * 

Triangle:
* 
* * 
* * * 
* * * * 

Number pyramid:
1 
1 2 
1 2 3 
1 2 3 4 

Clock (hours:minutes):
00:00  00:15  00:30  00:45  
01:00  01:15  01:30  01:45  
02:00  02:15  02:30  02:45  
```
