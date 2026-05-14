package com.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Calculator {

    private static final List<String> history = new ArrayList<>();
    private static double memory = 0.0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("========== CALCULATOR ==========");
        System.out.println("Commands: basic, scientific, memory, history, clear, exit");

        while (true) {
            System.out.print("\n> ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "basic" -> basicMode(scanner);
                case "scientific" -> scientificMode(scanner);
                case "memory" -> memoryMode(scanner);
                case "history" -> showHistory();
                case "clear" -> { history.clear(); System.out.println("History cleared."); }
                case "exit" -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Unknown command. Try: basic, scientific, memory, history, clear, exit");
            }
        }
    }

    private static void basicMode(Scanner scanner) {
        System.out.println("\n--- Basic Mode ---");
        System.out.print("Enter expression (e.g., 2 + 3): ");
        double a = scanner.nextDouble();
        String op = scanner.next();
        double b = scanner.nextDouble();
        scanner.nextLine();

        double result = switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : Double.NaN;
            case "%" -> a % b;
            case "^" -> Math.pow(a, b);
            default -> { System.out.println("Invalid operator"); yield Double.NaN; }
        };

        String entry = a + " " + op + " " + b + " = " + result;
        history.add(entry);
        System.out.println("Result: " + (Double.isNaN(result) ? "Error (division by zero)" : result));
    }

    private static void scientificMode(Scanner scanner) {
        System.out.println("\n--- Scientific Mode ---");
        System.out.println("Functions: sin, cos, tan, log, ln, sqrt, abs, round, ceil, floor");
        System.out.print("Enter function and value: ");
        String func = scanner.next();
        double x = scanner.nextDouble();
        scanner.nextLine();

        double result = switch (func.toLowerCase()) {
            case "sin" -> Math.sin(Math.toRadians(x));
            case "cos" -> Math.cos(Math.toRadians(x));
            case "tan" -> Math.tan(Math.toRadians(x));
            case "log" -> Math.log10(x);
            case "ln" -> Math.log(x);
            case "sqrt" -> Math.sqrt(x);
            case "abs" -> Math.abs(x);
            case "round" -> Math.round(x);
            case "ceil" -> Math.ceil(x);
            case "floor" -> Math.floor(x);
            default -> { System.out.println("Unknown function"); yield Double.NaN; }
        };

        String entry = func + "(" + x + ") = " + result;
        history.add(entry);
        System.out.println("Result: " + result);
    }

    private static void memoryMode(Scanner scanner) {
        System.out.println("\n--- Memory Mode ---");
        System.out.println("Current memory: " + memory);
        System.out.print("Enter value (or 0 to clear): ");
        double value = scanner.nextDouble();
        scanner.nextLine();
        memory = value;
        System.out.println("Memory set to: " + memory);
    }

    private static void showHistory() {
        System.out.println("\n--- History ---");
        if (history.isEmpty()) {
            System.out.println("No calculations yet.");
        } else {
            for (int i = 0; i < history.size(); i++) {
                System.out.println((i + 1) + ". " + history.get(i));
            }
        }
    }
}
