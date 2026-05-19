# Lesson 15: String Methods

## Key Concepts
- `String` is an object with many useful methods
- **Query methods**: `length()`, `charAt()`, `indexOf()`, `lastIndexOf()`
- **Transformation methods**: `toUpperCase()`, `toLowerCase()`, `trim()`, `replace()`
- **Boolean checks**: `contains()`, `startsWith()`, `endsWith()`, `equals()`, `equalsIgnoreCase()`, `isEmpty()`
- **Utility methods**: `split()`, `toCharArray()`

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        String name = "Bro Code";

        System.out.println("Original: " + name);
        System.out.println("Length: " + name.length());
        System.out.println("Char at 0: " + name.charAt(0));
        System.out.println("Index of 'C': " + name.indexOf('C'));
        System.out.println("Last index of 'o': " + name.lastIndexOf('o'));

        System.out.println("Uppercase: " + name.toUpperCase());
        System.out.println("Lowercase: " + name.toLowerCase());
        System.out.println("Trim: '" + "  Hello  ".trim() + "'");
        System.out.println("Replace: " + name.replace('o', 'a'));
        System.out.println("Contains 'Code': " + name.contains("Code"));
        System.out.println("Starts with 'Bro': " + name.startsWith("Bro"));
        System.out.println("Ends with 'de': " + name.endsWith("de"));
        System.out.println("Equals 'bro code': " + name.equals("bro code"));
        System.out.println("Equals ignore case: " + name.equalsIgnoreCase("bro code"));
        System.out.println("Is empty: " + name.isEmpty());

        String sentence = "apple,banana,orange";
        String[] fruits = sentence.split(",");
        System.out.println("\nSplit by comma:");
        for (String fruit : fruits) {
            System.out.println("  " + fruit);
        }

        char[] chars = name.toCharArray();
        System.out.println("\nTo char array:");
        for (char c : chars) {
            System.out.print(c + " ");
        }
        System.out.println();
    }
}
```

### Explanation
| Method | Purpose | Example Result |
|---|---|---|
| `length()` | Number of characters | `8` |
| `charAt(i)` | Character at index `i` | `'B'` |
| `indexOf(c)` | First occurrence of `c` | `4` (for `'C'`) |
| `lastIndexOf(c)` | Last occurrence of `c` | `7` (for `'o'`) |
| `toUpperCase()` | All uppercase | `"BRO CODE"` |
| `toLowerCase()` | All lowercase | `"bro code"` |
| `trim()` | Remove leading/trailing spaces | `"Hello"` |
| `replace(old, new)` | Replace all matching chars | `"Bra Cade"` |
| `contains(s)` | Check if substring exists | `true` |
| `startsWith(s)` | Check prefix | `true` |
| `endsWith(s)` | Check suffix | `true` |
| `equals(s)` | Exact match (case-sensitive) | `false` |
| `equalsIgnoreCase(s)` | Case-insensitive match | `true` |
| `isEmpty()` | Check if length is 0 | `false` |
| `split(regex)` | Split into array by delimiter | `["apple","banana","orange"]` |
| `toCharArray()` | Convert to `char[]` array | `['B','r','o',' ','C','o','d','e']` |

## Expected Output

```
Original: Bro Code
Length: 8
Char at 0: B
Index of 'C': 4
Last index of 'o': 7
Uppercase: BRO CODE
Lowercase: bro code
Trim: 'Hello'
Replace: Bra Cade
Contains 'Code': true
Starts with 'Bro': true
Ends with 'de': true
Equals 'bro code': false
Equals ignore case: true
Is empty: false

Split by comma:
  apple
  banana
  orange

To char array:
B r o   C o d e 
```
