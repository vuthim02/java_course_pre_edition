# Lesson 45: OOP — Classes and Objects

## Key Concepts
- **Class**: a blueprint for creating objects (defines fields and methods)
- **Object**: an instance of a class created with the `new` keyword
- **Fields** (instance variables): attributes that store the state of an object
- **Constructor**: a special method that runs when an object is created; initializes fields
- The `this` keyword distinguishes instance variables from constructor parameters
- Multiple objects can be created from the same class, each with its own state
- Calling methods on objects with dot notation: `car1.drive()`

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Car car1 = new Car("Tesla", "Model 3", 2024, "Red");
        Car car2 = new Car("Ford", "Mustang", 2023, "Blue");

        System.out.println("Car 1:");
        System.out.println("Make: " + car1.make);
        System.out.println("Model: " + car1.model);
        System.out.println("Year: " + car1.year);
        System.out.println("Color: " + car1.color);
        car1.drive();
        car1.brake();

        System.out.println("\nCar 2:");
        System.out.println("Make: " + car2.make);
        System.out.println("Model: " + car2.model);
        car2.drive();
        car2.brake();
    }
}

class Car {
    String make;
    String model;
    int year;
    String color;

    Car(String make, String model, int year, String color) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
    }

    void drive() {
        System.out.println("You drive the " + model);
    }

    void brake() {
        System.out.println("You step on the brakes");
    }
}
```

## Explanation
1. The `Car` class defines four **fields** (`make`, `model`, `year`, `color`) that each `Car` object will have its own copy of.
2. The **constructor** `Car(String make, String model, int year, String color)` has the same name as the class and no return type. It uses `this` to differentiate between the parameter `make` and the field `this.make`.
3. `new Car("Tesla", "Model 3", 2024, "Red")` calls the constructor, which initializes the new object's fields with the provided values.
4. Each object (`car1`, `car2`) is independent — changing `car1`'s fields does not affect `car2`.
5. Methods `drive()` and `brake()` define behaviors that all `Car` objects share. They are called with dot notation.
6. The `Car` class is defined in the same file as `Main` (only one `public` class per file is allowed; `Car` has package-private access).

## Expected Output

```
Car 1:
Make: Tesla
Model: Model 3
Year: 2024
Color: Red
You drive the Model 3
You step on the brakes

Car 2:
Make: Ford
Model: Mustang
You drive the Mustang
You step on the brakes
```
