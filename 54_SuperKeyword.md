# Lesson 54: The `super` Keyword

## Key Concepts
- `super` refers to the **parent class** (superclass)
- `super()` calls the parent class constructor (must be the first statement in a constructor)
- `super.methodName()` calls an overridden method from the parent class
- Use `super` to access parent fields or methods that are hidden by the subclass
- `super` is essential for building on parent behavior rather than replacing it entirely

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Student student = new Student("Alice", 20, "Computer Science");
        student.displayInfo();
        student.study();

        System.out.println();

        Employee emp = new Employee("Bob", 30, 50000);
        emp.displayInfo();
        emp.work();
    }
}

class Person {
    String name;
    int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    void displayInfo() {
        System.out.println("Name: " + name + ", Age: " + age);
    }
}

class Student extends Person {
    String major;

    Student(String name, int age, String major) {
        super(name, age);
        this.major = major;
    }

    @Override
    void displayInfo() {
        super.displayInfo();
        System.out.println("Major: " + major);
    }

    void study() {
        System.out.println(name + " is studying " + major + ".");
    }
}

class Employee extends Person {
    double salary;

    Employee(String name, int age, double salary) {
        super(name, age);
        this.salary = salary;
    }

    @Override
    void displayInfo() {
        super.displayInfo();
        System.out.println("Salary: $" + salary);
    }

    void work() {
        System.out.println(name + " is working.");
    }
}
```

### Explanation
In both `Student` and `Employee`, `super(name, age)` passes data to the `Person` constructor. The overridden `displayInfo()` methods call `super.displayInfo()` first to print the shared name/age line, then add their own class-specific information. This avoids duplicating the parent's code.

### Expected Output
```
Name: Alice, Age: 20
Major: Computer Science
Alice is studying Computer Science.

Name: Bob, Age: 30
Salary: $50000.0
Bob is working.
```
