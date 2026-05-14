# Core Java Advanced — Lesson 5: Reflection API

## What is Reflection?

**Reflection** allows a program to **inspect and modify itself at runtime** — examining classes, methods, fields, and annotations dynamically.

```java
// Without reflection — compile-time, known types:
String s = "hello";
s.toUpperCase();  // We KNOW s is a String

// With reflection — runtime discovery:
Object obj = getUnknownObject();
Class<?> clazz = obj.getClass();  // Discover type at runtime!
Method method = clazz.getMethod("toUpperCase");
String result = (String) method.invoke(obj);  // Call method dynamically!
```

## Getting Class Objects

```java
// Method 1: .class syntax (compile-time)
Class<String> stringClass = String.class;

// Method 2: getClass() (runtime)
String s = "hello";
Class<?> clazz = s.getClass();

// Method 3: Class.forName() (fully dynamic)
Class<?> clazz = Class.forName("java.util.ArrayList");

// Method 4: Primitive types
Class<Integer> intClass = int.class;
Class<Void> voidClass = void.class;
```

## Inspecting Classes

```java
Class<?> clazz = String.class;

// Class info
System.out.println(clazz.getName());         // java.lang.String
System.out.println(clazz.getSimpleName());   // String
System.out.println(clazz.getPackageName());  // java.lang
System.out.println(clazz.getModifiers());    // public final

// Superclass and interfaces
System.out.println(clazz.getSuperclass());   // java.lang.Object
Class<?>[] interfaces = clazz.getInterfaces();  // Serializable, Comparable, CharSequence

// Constructors
Constructor<?>[] constructors = clazz.getConstructors();
for (Constructor<?> c : constructors) {
    System.out.println("Constructor: " + c);
}

// Methods
Method[] methods = clazz.getMethods();  // All public methods
Method[] declaredMethods = clazz.getDeclaredMethods();  // All methods (including private)

// Fields
Field[] fields = clazz.getFields();  // All public fields
Field[] declaredFields = clazz.getDeclaredFields();  // All fields
```

## Inspecting and Invoking Methods

```java
public class Calculator {
    private int add(int a, int b) { return a + b; }
    public String greet(String name) { return "Hello, " + name; }
}

// Find and invoke a method
Calculator calc = new Calculator();
Class<?> clazz = calc.getClass();

// Find public method
Method greetMethod = clazz.getMethod("greet", String.class);
String result = (String) greetMethod.invoke(calc, "Alice");
System.out.println(result);  // "Hello, Alice"

// Find and invoke PRIVATE method
Method addMethod = clazz.getDeclaredMethod("add", int.class, int.class);
addMethod.setAccessible(true);  // Suppress Java access control
int sum = (int) addMethod.invoke(calc, 5, 3);
System.out.println(sum);  // 8
```

## Accessing and Modifying Fields

```java
public class Person {
    private String name = "Default";
}

// Read private field
Person person = new Person();
Field field = Person.class.getDeclaredField("name");
field.setAccessible(true);
String name = (String) field.get(person);  // "Default"

// Modify private field
field.set(person, "Alice");
System.out.println(person.getName());  // "Alice"
```

## Working with Annotations via Reflection

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotNull { }

public class User {
    @NotNull
    private String name;
    private String email;
}

// Validation framework
public class Validator {
    public static void validate(Object obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(NotNull.class)) {
                field.setAccessible(true);
                if (field.get(obj) == null) {
                    throw new RuntimeException(field.getName() + " is null!");
                }
            }
        }
    }
}
```

## Reflection & Generics

```java
// Get generic type information
public class MyList extends ArrayList<String> { }

ParameterizedType type = (ParameterizedType) MyList.class.getGenericSuperclass();
Type[] typeArgs = type.getActualTypeArguments();
System.out.println(typeArgs[0]);  // class java.lang.String
```

## Performance Considerations

Reflection is **SLOW** — avoid in hot paths:
- ~50-100x slower than direct calls
- Disables JIT optimizations
- setAccessible bypasses security

**Use reflection for:** Frameworks, libraries, testing tools, serialization
**Avoid reflection for:** Performance-critical business logic

---

### Exercises

1. Write a method that prints ALL methods of the `String` class.
2. Create a private field in a class and modify it via reflection.
3. Use `Class.forName()` to dynamically load a class by name.
4. Build a simple JSON serializer that reads object fields via reflection and converts them to JSON.
5. Build a validation framework using custom annotations + reflection.
