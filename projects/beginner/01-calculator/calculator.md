# Calculator — Complete Java Source Code

File: `Calculator.java`

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A console-based calculator with basic arithmetic, scientific functions,
 * memory operations, and calculation history.
 */
public class Calculator {

    private static final List<String> history = new ArrayList<>();
    private static double memory = 0.0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("========== CALCULATOR ==========");
        System.out.println("Commands: basic, scientific, memory, history, clear, exit");

        while (true) {
            // Display prompt and read user command
            System.out.print("\n> ");
            String command = scanner.nextLine().trim().toLowerCase();

            // Dispatch command using switch expression
            switch (command) {
                case "exit" -> {
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                }
                case "basic" -> basicMode(scanner);
                case "scientific" -> scientificMode(scanner);
                case "memory" -> memoryMode(scanner);
                case "history" -> showHistory();
                case "clear" -> {
                    history.clear();
                    System.out.println("History cleared.");
                }
                default -> System.out.println(
                    "Available: basic, scientific, memory, history, clear, exit");
            }
        }
    }

    /** Handles basic arithmetic: <num> <op> <num> */
    private static void basicMode(Scanner scanner) {
        System.out.println("Basic mode — enter: <num> <op> <num>");
        System.out.println("Operators: +  -  *  /  %");
        System.out.print("  > ");
        String line = scanner.nextLine().trim();
        // Split on whitespace
        String[] parts = line.split("\\s+");

        if (parts.length != 3) {
            System.out.println("  Invalid format. Use: number operator number");
            return;
        }

        try {
            double a = Double.parseDouble(parts[0]);
            double b = Double.parseDouble(parts[2]);
            String op = parts[1];

            // Perform the operation using a switch expression
            double result = switch (op) {
                case "+" -> a + b;
                case "-" -> a - b;
                case "*" -> a * b;
                case "/" -> {
                    if (b == 0.0) throw new ArithmeticException("Division by zero");
                    yield a / b;
                }
                case "%" -> a % b;
                default -> throw new IllegalArgumentException("Unknown operator: " + op);
            };

            // Record in history and display
            String entry = String.format("%.2f %s %.2f = %.2f", a, op, b, result);
            history.add(entry);
            System.out.println("  " + entry);

        } catch (NumberFormatException e) {
            System.out.println("  Invalid number format.");
        } catch (ArithmeticException e) {
            System.out.println("  Math error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("  " + e.getMessage());
        }
    }

    /** Handles scientific functions: sin, cos, tan, log, sqrt, pow */
    private static void scientificMode(Scanner scanner) {
        System.out.println("Scientific mode — enter: <func> <num>");
        System.out.println("Functions: sin, cos, tan, log, sqrt, pow");
        System.out.println("  For pow: pow <base> <exponent>");
        System.out.print("  > ");
        String line = scanner.nextLine().trim();
        String[] parts = line.split("\\s+");

        if (parts.length < 2) {
            System.out.println("  Invalid format. Use: function number");
            return;
        }

        try {
            String func = parts[0].toLowerCase();
            String result;

            switch (func) {
                // Single-argument functions
                case "sin", "cos", "tan", "log", "sqrt" -> {
                    if (parts.length != 2) {
                        System.out.println("  This function takes one argument.");
                        return;
                    }
                    double x = Double.parseDouble(parts[1]);
                    double val = switch (func) {
                        case "sin" -> Math.sin(Math.toRadians(x));
                        case "cos" -> Math.cos(Math.toRadians(x));
                        case "tan" -> Math.tan(Math.toRadians(x));
                        case "log" -> Math.log(x);          // natural log
                        case "sqrt" -> Math.sqrt(x);
                        // Unreachable, but required for exhaustiveness
                        default -> throw new IllegalStateException();
                    };
                    result = String.format("%s(%.2f) = %.4f", func, x, val);
                }
                // Two-argument function: pow
                case "pow" -> {
                    if (parts.length != 3) {
                        System.out.println("  Usage: pow base exponent");
                        return;
                    }
                    double base = Double.parseDouble(parts[1]);
                    double exp = Double.parseDouble(parts[2]);
                    double val = Math.pow(base, exp);
                    result = String.format("pow(%.2f, %.2f) = %.4f", base, exp, val);
                }
                default -> {
                    System.out.println("  Unknown function: " + func);
                    return;
                }
            }

            history.add("[sci] " + result);
            System.out.println("  " + result);

        } catch (NumberFormatException e) {
            System.out.println("  Invalid number format.");
        }
    }

    /** Handles memory operations: M+, M-, MR, MC */
    private static void memoryMode(Scanner scanner) {
        System.out.println("Memory mode — commands: M+, M-, MR, MC");
        System.out.print("  > ");
        String cmd = scanner.nextLine().trim().toUpperCase();

        switch (cmd) {
            case "M+" -> {
                System.out.print("  Enter value to add to memory: ");
                double v = Double.parseDouble(scanner.nextLine().trim());
                memory += v;
                System.out.println("  Memory = " + memory);
            }
            case "M-" -> {
                System.out.print("  Enter value to subtract: ");
                double v = Double.parseDouble(scanner.nextLine().trim());
                memory -= v;
                System.out.println("  Memory = " + memory);
            }
            case "MR" -> System.out.println("  Memory = " + memory);
            case "MC" -> {
                memory = 0.0;
                System.out.println("  Memory cleared.");
            }
            default -> System.out.println("  Commands: M+, M-, MR, MC");
        }
    }

    /** Displays all past calculations */
    private static void showHistory() {
        if (history.isEmpty()) {
            System.out.println("  No history yet.");
            return;
        }
        System.out.println("  --- History ---");
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, history.get(i));
        }
        System.out.println("  ---------------");
    }
}
```
