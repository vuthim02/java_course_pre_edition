# Abstract Classes and Interfaces

Abstract classes can hold state, constructors, and both abstract and concrete methods. Interfaces define contracts; since Java 8+ they can also have `default`, `static`, and `private` methods. A class can extend at most one abstract class but implement many interfaces. Use abstract classes for related classes sharing state/behavior; use interfaces for unrelated classes sharing a capability.

```java
import java.util.List;
import java.util.ArrayList;

// ============================================================
// Abstract class — shared state + partial implementation
// ============================================================

abstract class Shape {
    protected String color;

    public Shape(String color) {
        this.color = color;
    }

    // Abstract methods — subclasses must implement
    public abstract double area();
    public abstract double perimeter();

    // Concrete method — inherited as-is
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // Concrete method using abstract methods
    public void printInfo() {
        System.out.printf("%s %s: area=%.2f, perimeter=%.2f%n",
            color, getClass().getSimpleName(), area(), perimeter());
    }
}

class Circle extends Shape {
    private double radius;

    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }

    @Override
    public double perimeter() {
        return 2 * Math.PI * radius;
    }
}

class Rectangle extends Shape {
    private double width, height;

    public Rectangle(String color, double width, double height) {
        super(color);
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }

    @Override
    public double perimeter() {
        return 2 * (width + height);
    }
}

// ============================================================
// Interface — traditional and modern features
// ============================================================

interface Drawable {
    // Abstract method (implicitly public abstract)
    void draw();

    // Default method (Java 8+) — inherited unless overridden
    default void printDimensions() {
        System.out.println("No dimensions available by default");
    }

    // Static method in interface (Java 8+)
    static String getDrawingTool() {
        return "Java Graphics";
    }

    // Private method in interface (Java 9+) — helper for default methods
    private void log(String msg) {
        System.out.println("[LOG] " + msg);
    }

    // Private static method in interface (Java 9+)
    private static String version() {
        return "1.0";
    }
}

interface Resizable {
    void resize(double factor);
}

// ============================================================
// Multiple interface implementation
// ============================================================

class DrawableCircle extends Shape implements Drawable, Resizable {
    private double radius;

    public DrawableCircle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double area() { return Math.PI * radius * radius; }

    @Override
    public double perimeter() { return 2 * Math.PI * radius; }

    @Override
    public void draw() {
        System.out.println("Drawing a " + color + " circle with radius " + radius);
    }

    @Override
    public void resize(double factor) {
        radius *= factor;
    }

    @Override
    public void printDimensions() {
        System.out.println("Circle radius = " + radius);
    }
}

// ============================================================
// Interface extending another interface
// ============================================================

interface Colorful {
    String getColor();
}

interface ColoredDrawable extends Colorful, Drawable {
    // inherits getColor() and draw()
}

class ColoredSquare implements ColoredDrawable {
    private String color;
    private double side;

    ColoredSquare(String color, double side) {
        this.color = color;
        this.side = side;
    }

    @Override
    public String getColor() { return color; }

    @Override
    public void draw() {
        System.out.println("Drawing a " + color + " square");
    }
}

// ============================================================
// @FunctionalInterface (SAM — Single Abstract Method)
// ============================================================

@FunctionalInterface
interface Greeter {
    String greet(String name);
    // Only ONE abstract method allowed
    // default and static methods don't count
}

// ============================================================
// Main
// ============================================================

public class AbstractVsInterfaceDemo {
    public static void main(String[] args) {
        // --- Abstract class ---
        Shape s1 = new Circle("red", 5);
        Shape s2 = new Rectangle("blue", 3, 4);
        s1.printInfo();
        s2.printInfo();

        // --- Multiple interfaces ---
        DrawableCircle dc = new DrawableCircle("green", 3);
        dc.draw();
        dc.resize(2);
        dc.printDimensions();
        System.out.println("Drawing tool: " + Drawable.getDrawingTool());

        // --- Interface extending interface ---
        ColoredDrawable cs = new ColoredSquare("yellow", 4);
        cs.draw();
        System.out.println("Color: " + cs.getColor());

        // --- FunctionalInterface as lambda ---
        Greeter g = name -> "Hello, " + name + "!";
        System.out.println(g.greet("Alice"));
    }
}
```
