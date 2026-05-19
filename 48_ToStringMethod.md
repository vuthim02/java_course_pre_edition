# Lesson 48: The `toString()` Method

## Key Concepts
- `toString()` is a method inherited from the `Object` class
- It returns a string representation of an object
- By default, it returns the class name + hash code (e.g., `Student@2f92e0f4`)
- Override `toString()` to display meaningful object data
- The `@Override` annotation tells the compiler you are overriding a parent method
- When you print an object with `System.out.println()`, Java automatically calls `toString()`

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Student student1 = new Student("Alice", 20, 'A');
        Student student2 = new Student("Bob", 22, 'B');

        System.out.println(student1);
        System.out.println(student2);

        Car myCar = new Car("Toyota", "Camry", 2022);
        System.out.println(myCar);
    }
}

class Student {
    String name;
    int age;
    char grade;

    Student(String name, int age, char grade) {
        this.name = name;
        this.age = age;
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "Student: " + name + ", Age: " + age + ", Grade: " + grade;
    }
}

class Car {
    String make;
    String model;
    int year;

    Car(String make, String model, int year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }

    @Override
    public String toString() {
        return year + " " + make + " " + model;
    }
}
```

### Explanation
`Student` and `Car` both override `toString()` to return a human-readable string. When passed to `println`, the overridden method is called automatically.

### Expected Output
```
Student: Alice, Age: 20, Grade: A
Student: Bob, Age: 22, Grade: B
2022 Toyota Camry
```
