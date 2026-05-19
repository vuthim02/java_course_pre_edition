# Lesson 57: Access Modifiers

## Key Concepts
- Access modifiers control where classes, methods, and fields are visible
- **`public`** — accessible from **anywhere**
- **`protected`** — accessible within the **same package** and by **subclasses** (even in other packages)
- **`default`** (no modifier) — accessible only within the **same package**
- **`private`** — accessible only **within the same class**
- Use the most restrictive access level possible for good encapsulation

## Code Example

### AccessExample.java
```java
public class AccessExample {
    public int publicVar = 1;
    int defaultVar = 2;
    protected int protectedVar = 3;
    private int privateVar = 4;

    public void publicMethod() {
        System.out.println("Public method - accessible everywhere");
    }

    void defaultMethod() {
        System.out.println("Default method - accessible within package");
    }

    protected void protectedMethod() {
        System.out.println("Protected method - accessible within package and subclasses");
    }

    private void privateMethod() {
        System.out.println("Private method - accessible only within this class");
    }

    public void accessPrivate() {
        System.out.println("Accessing private from within class: " + privateVar);
        privateMethod();
    }
}
```

### Main.java
```java
public class Main {
    public static void main(String[] args) {
        AccessExample example = new AccessExample();

        System.out.println("Public variable: " + example.publicVar);
        System.out.println("Default variable: " + example.defaultVar);
        System.out.println("Protected variable: " + example.protectedVar);

        example.publicMethod();
        example.defaultMethod();
        example.protectedMethod();
    }
}
```

### Explanation
`Main` can access `publicVar`, `defaultVar`, and `protectedVar` because all classes are in the same package. `privateVar` and `privateMethod()` are only accessible from within `AccessExample` itself (as demonstrated by `accessPrivate()`). If `Main` were in a different package, only the `public` members would be visible.

### Expected Output
```
Public variable: 1
Default variable: 2
Protected variable: 3
Public method - accessible everywhere
Default method - accessible within package
Protected method - accessible within package and subclasses
```

### Summary Table
| Modifier    | Same Class | Same Package | Subclass (different pkg) | Anywhere |
|-------------|:----------:|:------------:|:------------------------:|:--------:|
| `private`   | Yes        | No           | No                       | No       |
| `default`   | Yes        | Yes          | No                       | No       |
| `protected` | Yes        | Yes          | Yes                      | No       |
| `public`    | Yes        | Yes          | Yes                      | Yes      |
