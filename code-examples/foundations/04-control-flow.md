# 04 — Control Flow

Conditional statements (`if`/`else`, `switch`), loops (`for`, `while`, `do-while`), `break`/`continue`, and nested loops.

## if / else if / else Ladder

```java
// IfElseDemo.java — all forms of if-else
package com.example;

public class IfElseDemo {
    public static void main(String[] args) {
        int score = 83;

        // Simple if
        if (score >= 60) {
            System.out.println("Passed");
        }

        // if-else
        if (score >= 90) {
            System.out.println("Grade: A");
        } else {
            System.out.println("Grade: B or below");
        }

        // if-else-if-else ladder
        char grade;
        if (score >= 90) {
            grade = 'A';
        } else if (score >= 80) {
            grade = 'B';
        } else if (score >= 70) {
            grade = 'C';
        } else if (score >= 60) {
            grade = 'D';
        } else {
            grade = 'F';
        }
        System.out.println("Score " + score + " → Grade " + grade);
    }
}
```

## Traditional Switch (with `break`)

```java
// SwitchTraditional.java — pre-Java-14 switch with break
package com.example;

public class SwitchTraditional {
    public static void main(String[] args) {
        int dayNum = 3;
        String dayName;

        // Traditional switch — fall-through requires break!
        switch (dayNum) {
            case 1:
                dayName = "Monday";
                break;
            case 2:
                dayName = "Tuesday";
                break;
            case 3:
                dayName = "Wednesday";
                break;
            case 4:
                dayName = "Thursday";
                break;
            case 5:
                dayName = "Friday";
                break;
            case 6:
                dayName = "Saturday";
                break;
            case 7:
                dayName = "Sunday";
                break;
            default:
                dayName = "Invalid day";
        }
        System.out.println("Day " + dayNum + " is " + dayName);

        // Fall-through can be useful (grouping cases)
        String type;
        switch (dayNum) {
            case 1: case 2: case 3: case 4: case 5:
                type = "Weekday";
                break;
            case 6: case 7:
                type = "Weekend";
                break;
            default:
                type = "Invalid";
        }
        System.out.println("It's a " + type);
    }
}
```

## Arrow Switch (`->`) — Java 14+

```java
// SwitchArrow.java — Java 14+ arrow syntax (no fall-through)
package com.example;

public class SwitchArrow {
    public static void main(String[] args) {
        int dayNum = 5;

        // Arrow switch — no break needed, no fall-through
        String dayName = switch (dayNum) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "Invalid day";
        };
        System.out.println("Day " + dayNum + " → " + dayName);

        // Arrow switch with blocks (for multiple statements)
        String description = switch (dayNum) {
            case 1, 2, 3, 4, 5 -> {
                System.out.println("   (calculating weekday...)");
                yield "Weekday";
            }
            case 6, 7 -> {
                System.out.println("   (calculating weekend...)");
                yield "Weekend";
            }
            default -> "Invalid";
        };
        System.out.println("Type: " + description);
    }
}
```

## Switch Expression (`yield`) — Java 14+

```java
// SwitchYield.java — switch used as an expression with yield
package com.example;

public class SwitchYield {
    public static void main(String[] args) {
        enum Season { SPRING, SUMMER, FALL, WINTER }
        Season season = Season.SUMMER;

        // Switch expression with yield — can assign directly to a variable
        String description = switch (season) {
            case SPRING -> "Flowers bloom";
            case SUMMER -> "Hot and sunny";
            case FALL   -> "Leaves fall";
            case WINTER -> "Cold and snowy";
        };
        System.out.println(season + ": " + description);

        // yield for multi-line branches inside a switch expression
        int month = 7;
        int daysInMonth = switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> {
                // February — in a real program you'd check for leap year
                System.out.println("   February: checking leap year...");
                yield 28;   // non-leap year
            }
            default -> throw new IllegalArgumentException("Invalid month: " + month);
        };
        System.out.println("Month " + month + " has " + daysInMonth + " days");
    }
}
```

## Pattern Matching for Switch — Java 21+

```java
// SwitchPatternMatching.java — Java 21+ pattern matching in switch
package com.example;

sealed interface Shape permits Circle, Rectangle, Triangle {}
record Circle(double radius) implements Shape {}
record Rectangle(double w, double h) implements Shape {}
record Triangle(double base, double height) implements Shape {}

public class SwitchPatternMatching {
    public static void main(String[] args) {
        System.out.println("Area of circle(5):    " + area(new Circle(5)));
        System.out.println("Area of rect(3,4):    " + area(new Rectangle(3, 4)));
        System.out.println("Area of tri(6,3):     " + area(new Triangle(6, 3)));
        System.out.println("Area of null:         " + area(null));
    }

    // Pattern matching switch — no explicit casting needed!
    static double area(Shape shape) {
        return switch (shape) {
            case Circle c      -> Math.PI * c.radius() * c.radius();
            case Rectangle r   -> r.w() * r.h();
            case Triangle t    -> 0.5 * t.base() * t.height();
            case null          -> 0;       // null case handled explicitly
            // default not needed with sealed interface + exhaustive cases
        };
    }

    // Guarded patterns (when clause)
    static String classify(Shape shape) {
        return switch (shape) {
            case Circle c when c.radius() > 10 -> "Large circle";
            case Circle c                      -> "Circle (radius=" + c.radius() + ")";
            case Rectangle r when r.w() == r.h() -> "Square!";
            case Rectangle r -> "Rectangle " + r.w() + "×" + r.h();
            case Triangle t  -> "Triangle";
            case null        -> "Nothing";
        };
    }
}
```

## Loops — `for`, `while`, `do-while`

```java
// LoopDemo.java — all loop forms
package com.example;

import java.util.List;

public class LoopDemo {
    public static void main(String[] args) {
        // ── for (traditional) ──
        System.out.println("Traditional for:");
        for (int i = 0; i < 5; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        // ── for-each (enhanced for) ──
        System.out.println("Enhanced for:");
        int[] numbers = {10, 20, 30, 40, 50};
        for (int n : numbers) {
            System.out.print(n + " ");
        }
        System.out.println();

        // ── for-each with collections ──
        List<String> names = List.of("Alice", "Bob", "Charlie");
        for (String name : names) {
            System.out.println("  Hello, " + name);
        }

        // ── while ──
        System.out.println("while loop:");
        int count = 0;
        while (count < 5) {
            System.out.print(count + " ");
            count++;
        }
        System.out.println();

        // ── do-while (runs at least once) ──
        System.out.println("do-while loop:");
        int x = 0;
        do {
            System.out.print(x + " ");
            x++;
        } while (x < 5);
        System.out.println();

        // ── Infinite loops (commented out — would hang) ──
        // for (;;) { ... }           // infinite for loop
        // while (true) { ... }       // infinite while loop
    }
}
```

## `break`, `continue`, Labeled Break/Continue

```java
// BreakContinueDemo.java — controlling loop flow
package com.example;

public class BreakContinueDemo {
    public static void main(String[] args) {
        // ── break: exit the innermost loop ──
        System.out.println("break example:");
        for (int i = 0; i < 10; i++) {
            if (i == 5) break;       // stop when i reaches 5
            System.out.print(i + " ");
        }
        System.out.println("(broke at 5)");

        // ── continue: skip the rest of this iteration ──
        System.out.println("continue example:");
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) continue;   // skip even numbers
            System.out.print(i + " ");
        }
        System.out.println("(odds only)");

        // ── Labeled break: exit an outer loop ──
        System.out.println("Labeled break:");
        outer:
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                if (row == 2 && col == 3) {
                    System.out.print("(hit 2,3) ");
                    break outer;          // breaks BOTH loops
                }
                System.out.print("[" + row + "," + col + "]");
            }
            System.out.println();   // newline after each row
        }
        System.out.println("  — Exited outer loop");

        // ── Labeled continue: skip to next iteration of outer loop ──
        System.out.println("\nLabeled continue:");
        outer:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (i == j) {
                    System.out.print("skip(" + i + "," + j + ") ");
                    continue outer;       // go to next i, reset inner loop
                }
                System.out.print("(" + i + "," + j + ")");
            }
            System.out.println();
        }
        System.out.println();
    }
}
```

## Nested Loops

```java
// NestedLoopsDemo.java — loops inside loops
package com.example;

public class NestedLoopsDemo {
    public static void main(String[] args) {
        // ── Multiplication table ──
        System.out.println("Multiplication table (3×3):");
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                System.out.printf("%3d", i * j);
            }
            System.out.println();
        }

        // ── Triangle pattern ──
        System.out.println("\nTriangle pattern:");
        int rows = 5;
        for (int i = 1; i <= rows; i++) {
            // print spaces
            for (int s = 0; s < rows - i; s++) {
                System.out.print(" ");
            }
            // print stars
            for (int j = 0; j < i; j++) {
                System.out.print("* ");
            }
            System.out.println();
        }

        // ── 2D array traversal ──
        System.out.println("\n2D array traversal:");
        int[][] grid = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        int sum = 0;
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                sum += grid[r][c];
                System.out.print(grid[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println("Sum of all elements: " + sum);
    }
}
```
