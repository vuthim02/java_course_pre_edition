# Lesson 56: Abstraction

## Key Concepts
- **Abstraction** hides implementation details and shows only essential features
- An **abstract class** is declared with the `abstract` keyword and **cannot be instantiated**
- Abstract classes can have both abstract methods (no body) and concrete methods (with body)
- Any concrete subclass **must implement** all abstract methods from the parent
- Abstract methods are declared with `abstract` and end with a semicolon (no `{}`)
- Abstract classes can have constructors, fields, and static methods

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Circle circle = new Circle(5.0);
        Rectangle rectangle = new Rectangle(4.0, 6.0);

        System.out.println("=== CIRCLE ===");
        circle.displayInfo();
        System.out.println("Area: " + circle.calculateArea());

        System.out.println("\n=== RECTANGLE ===");
        rectangle.displayInfo();
        System.out.println("Area: " + rectangle.calculateArea());

        System.out.println("\nNumber of shapes created: " + Shape.getShapeCount());
    }
}

abstract class Shape {
    String color;
    static int shapeCount = 0;

    Shape(String color) {
        this.color = color;
        shapeCount++;
    }

    abstract double calculateArea();

    void displayInfo() {
        System.out.println("This is a " + color + " " + getClass().getSimpleName());
    }

    static int getShapeCount() {
        return shapeCount;
    }
}

class Circle extends Shape {
    double radius;

    Circle(double radius) {
        super("red");
        this.radius = radius;
    }

    @Override
    double calculateArea() {
        return Math.PI * radius * radius;
    }
}

class Rectangle extends Shape {
    double width;
    double height;

    Rectangle(double width, double height) {
        super("blue");
        this.width = width;
        this.height = height;
    }

    @Override
    double calculateArea() {
        return width * height;
    }
}
```

### Explanation
`Shape` is abstract — you cannot write `new Shape()`. It defines `calculateArea()` as an abstract method that `Circle` and `Rectangle` must implement with their own formulas. The concrete method `displayInfo()` and the static `getShapeCount()` are inherited and used by both subclasses.

### Expected Output
```
=== CIRCLE ===
This is a red Circle
Area: 78.53981633974483

=== RECTANGLE ===
This is a blue Rectangle
Area: 24.0

Number of shapes created: 2
```
