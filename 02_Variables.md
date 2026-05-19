# Lesson 2: Variables

## Key Concepts
- Variables store data in memory
- **Primitive types**: `int`, `double`, `char`, `boolean`
- **Reference type**: `String`
- Declaring and initializing variables
- String concatenation with `+`

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        int age = 25;
        double price = 19.99;
        char grade = 'A';
        boolean isStudent = true;
        String name = "Bro Code";

        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Price: $" + price);
        System.out.println("Grade: " + grade);
        System.out.println("Is Student: " + isStudent);

        int x = 10;
        int y = 20;
        System.out.println("x = " + x + ", y = " + y);
    }
}
```

### Explanation
- `int` stores whole numbers (e.g., `25`, `10`, `20`)
- `double` stores decimal numbers (e.g., `19.99`)
- `char` stores a single character in single quotes (e.g., `'A'`)
- `boolean` stores `true` or `false`
- `String` stores text in double quotes (capital S — it's a class, not a primitive)
- The `+` operator concatenates strings with variables

## Expected Output

```
Name: Bro Code
Age: 25
Price: $19.99
Grade: A
Is Student: true
x = 10, y = 20
```
