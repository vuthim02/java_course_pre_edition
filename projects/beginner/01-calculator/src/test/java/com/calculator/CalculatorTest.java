package com.calculator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        clearHistory();
        setMemory(0.0);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        clearHistory();
        setMemory(0.0);
    }

    @SuppressWarnings("unchecked")
    private void clearHistory() {
        try {
            Field historyField = Calculator.class.getDeclaredField("history");
            historyField.setAccessible(true);
            List<String> history = (List<String>) historyField.get(null);
            history.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMemory(double value) {
        try {
            Field memoryField = Calculator.class.getDeclaredField("memory");
            memoryField.setAccessible(true);
            memoryField.setDouble(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void simulateInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    @Nested
    class BasicOperations {

        @Test
        void testBasicMode_Addition() {
            simulateInput("basic\n2 + 3\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("5.0") || output.contains("5.0"));
        }

        @Test
        void testBasicMode_Subtraction() {
            simulateInput("basic\n10 - 4\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("6.0"));
        }

        @Test
        void testBasicMode_Multiplication() {
            simulateInput("basic\n6 * 7\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("42.0"));
        }

        @Test
        void testBasicMode_Division() {
            simulateInput("basic\n15 / 3\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("5.0"));
        }

        @Test
        void testBasicMode_Remainder() {
            simulateInput("basic\n17 % 5\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("2.0"));
        }

        @Test
        void testBasicMode_Power() {
            simulateInput("basic\n2 ^ 10\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("1024.0"));
        }

        @Test
        void testBasicMode_DivisionByZero() {
            simulateInput("basic\n5 / 0\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("Error (division by zero)"));
        }

        @Test
        void testBasicMode_ZeroValues() {
            simulateInput("basic\n0 + 0\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("0.0"));
        }

        @Test
        void testBasicMode_NegativeNumbers() {
            simulateInput("basic\n-5 + -3\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("-8.0") || output.contains("-8.0"));
        }
    }

    @Nested
    class ScientificFunctions {

        @Test
        void testScientific_Sin() {
            simulateInput("scientific\nsin 90\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("1.0"));
        }

        @Test
        void testScientific_Cos() {
            simulateInput("scientific\ncos 0\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("1.0"));
        }

        @Test
        void testScientific_Tan() {
            simulateInput("scientific\ntan 45\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("0.999") || output.contains("1.0"));
        }

        @Test
        void testScientific_Log() {
            simulateInput("scientific\nlog 100\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("2.0"));
        }

        @Test
        void testScientific_Sqrt() {
            simulateInput("scientific\nsqrt 144\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("12.0"));
        }

        @Test
        void testScientific_SqrtNegative() {
            simulateInput("scientific\nsqrt -1\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("NaN"));
        }

        @Test
        void testScientific_Ln() {
            simulateInput("scientific\nln 1\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("0.0"));
        }

        @Test
        void testScientific_Abs() {
            simulateInput("scientific\nabs -42\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("42.0"));
        }

        @Test
        void testScientific_Round() {
            simulateInput("scientific\nround 3.7\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("4.0"));
        }

        @Test
        void testScientific_Ceil() {
            simulateInput("scientific\nceil 3.2\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("4.0"));
        }

        @Test
        void testScientific_Floor() {
            simulateInput("scientific\nfloor 3.9\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("3.0"));
        }
    }

    @Nested
    class MemoryOperations {

        @Test
        void testMemory_SetValue() {
            simulateInput("memory\n42.5\nhistory\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("42.5"));
        }

        @Test
        void testMemory_Clear() {
            simulateInput("memory\n0\nhistory\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("0.0"));
        }

        @Test
        void testMemory_ZeroValue() {
            simulateInput("memory\n0.0\nhistory\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("0.0"));
        }
    }

    @Nested
    class HistoryTracking {

        @Test
        void testHistory_ShowsEntries() {
            simulateInput("basic\n2 + 3\nhistory\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("2") && output.contains("3") && output.contains("5.0"));
        }

        @Test
        void testHistory_Empty() {
            simulateInput("history\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("No calculations yet."));
        }

        @Test
        void testHistory_Clear() {
            simulateInput("basic\n1 + 1\nclear\nhistory\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("No calculations yet.") || output.contains("History cleared."));
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void testUnknownCommand() {
            simulateInput("foobar\nexit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("Unknown command"));
        }

        @Test
        void testExit() {
            simulateInput("exit\n");
            Calculator.main(new String[]{});
            String output = outContent.toString();
            assertTrue(output.contains("Goodbye"));
        }
    }
}
