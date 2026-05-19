# Lesson 46: Constructors

## Key Concepts
- A **constructor** is a special method that is called when an object is instantiated
- Constructors have the same name as the class and no return type
- The `this` keyword refers to the current object's instance variables
- Constructors are used to initialize object state (fields)
- Multiple objects can be created with different initial values using the same constructor

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Human human1 = new Human("Alice", 25, 65.5);
        Human human2 = new Human("Bob", 30, 80.0);

        System.out.println("Human 1:");
        human1.introduce();
        human1.eat();
        human1.drink();

        System.out.println("\nHuman 2:");
        human2.introduce();
        human2.eat();
    }
}

class Human {
    String name;
    int age;
    double weight;

    Human(String name, int age, double weight) {
        this.name = name;
        this.age = age;
        this.weight = weight;
    }

    void introduce() {
        System.out.println("Hi, I'm " + name + ". I'm " + age + " years old.");
    }

    void eat() {
        System.out.println(name + " is eating.");
    }

    void drink() {
        System.out.println(name + " is drinking.");
    }
}
```

### Explanation
The `Human` constructor accepts `name`, `age`, and `weight` parameters. The `this` keyword distinguishes between the parameter `name` and the field `this.name`. Each `Human` object gets its own copy of these fields.

### Expected Output
```
Human 1:
Hi, I'm Alice. I'm 25 years old.
Alice is eating.
Alice is drinking.

Human 2:
Hi, I'm Bob. I'm 30 years old.
Bob is eating.
```
