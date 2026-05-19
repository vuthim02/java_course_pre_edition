# Lesson 39: Wrapper Classes

## Key Concepts
- Wrapper classes provide an object representation of primitive types
- Autoboxing: automatic conversion from primitive to wrapper (`Integer intObj = 42;`)
- Unboxing: automatic conversion from wrapper to primitive (`int primitiveInt = intObj;`)
- Common wrappers: `Integer`, `Double`, `Boolean`, `Character`, `Long`, `Float`
- Utility methods: `parseInt()`, `parseDouble()`, `isDigit()`, `isLetter()`, `isWhitespace()`
- Constants: `Integer.MAX_VALUE`, `Integer.MIN_VALUE`
- Conversion methods: `toBinaryString()`, `toHexString()`

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Integer intObj = 42;
        Double doubleObj = 3.14;
        Boolean boolObj = true;
        Character charObj = 'A';
        Long longObj = 100L;
        Float floatObj = 2.5f;

        System.out.println("Wrapper class examples:");
        System.out.println("Integer: " + intObj);
        System.out.println("Double: " + doubleObj);
        System.out.println("Boolean: " + boolObj);
        System.out.println("Character: " + charObj);

        int primitiveInt = intObj;
        System.out.println("\nUnboxed int: " + primitiveInt);

        System.out.println("\nUtility methods:");
        System.out.println("Integer.parseInt('123'): " + Integer.parseInt("123"));
        System.out.println("Double.parseDouble('45.67'): " + Double.parseDouble("45.67"));
        System.out.println("Integer.MAX_VALUE: " + Integer.MAX_VALUE);
        System.out.println("Integer.MIN_VALUE: " + Integer.MIN_VALUE);
        System.out.println("Character.isDigit('5'): " + Character.isDigit('5'));
        System.out.println("Character.isLetter('A'): " + Character.isLetter('A'));
        System.out.println("Character.isWhitespace(' '): " + Character.isWhitespace(' '));

        String binary = Integer.toBinaryString(42);
        System.out.println("Binary of 42: " + binary);
        System.out.println("Hex of 255: " + Integer.toHexString(255));
    }
}
```

## Explanation
1. Autoboxing: writing `Integer intObj = 42` automatically creates an `Integer` object from the `int` literal 42. This works for all wrapper types.
2. Unboxing: assigning `Integer` to `int` automatically extracts the primitive value. This happens automatically in arithmetic and comparisons.
3. `Integer.parseInt("123")` converts a `String` to a primitive `int`. Useful for parsing user input. Throws `NumberFormatException` if the string is not a valid number.
4. `Integer.MAX_VALUE` (2147483647) and `Integer.MIN_VALUE` (-2147483648) are constants representing the range of the `int` type.
5. `Character.isDigit('5')` and `Character.isLetter('A')` check the category of a character.
6. `Integer.toBinaryString(42)` returns the binary representation (`"101010"`). `Integer.toHexString(255)` returns `"ff"`.

## Expected Output

```
Wrapper class examples:
Integer: 42
Double: 3.14
Boolean: true
Character: A

Unboxed int: 42

Utility methods:
Integer.parseInt('123'): 123
Double.parseDouble('45.67'): 45.67
Integer.MAX_VALUE: 2147483647
Integer.MIN_VALUE: -2147483648
Character.isDigit('5'): true
Character.isLetter('A'): true
Character.isWhitespace(' '): true
Binary of 42: 101010
Hex of 255: ff
```
