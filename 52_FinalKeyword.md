# Lesson 52: The `final` Keyword

## Key Concepts
- `final` makes a variable's value **unchangeable** (a constant)
- Convention: final variables are named in `UPPER_CASE` with underscores
- `final` with objects prevents the reference from being reassigned (but the object's internal state can still change)
- `final` can also be applied to methods (prevent overriding) and classes (prevent inheritance)
- The compiler will produce an error if you try to reassign a final variable

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        final double PI = 3.14159;
        System.out.println("PI = " + PI);

        final int MAX_USERS = 100;
        System.out.println("Max users: " + MAX_USERS);

        final String GREETING = "Hello";
        System.out.println(GREETING);

        final Student student = new Student("Alice");
        System.out.println(student.name);

        student.name = "Bob";
        System.out.println(student.name);

        System.out.println("\nAttempting to change final variable would cause error:");
        System.out.println("PI = 3.14; // not allowed!");
    }
}

class Student {
    String name;

    Student(String name) {
        this.name = name;
    }
}
```

### Explanation
`PI`, `MAX_USERS`, and `GREETING` are final primitives — their values cannot change. The `student` reference is `final`, meaning it cannot point to a different `Student` object, but the `name` field inside the object can still be modified because it is not `final`.

### Expected Output
```
PI = 3.14159
Max users: 100
Hello
Alice
Bob

Attempting to change final variable would cause error:
PI = 3.14; // not allowed!
```
