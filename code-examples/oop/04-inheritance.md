# Inheritance

Inheritance models an "is-a" relationship. The `extends` keyword derives a subclass from a superclass. `super()` calls the parent constructor; `super.method()` calls an overridden parent method. The `@Override` annotation catches mistakes at compile time. Prefer composition over inheritance when the relationship is "has-a" rather than "is-a".

```java
// ============================================================
// Base class
// ============================================================

class Animal {
    protected String name;   // accessible in subclasses

    public Animal(String name) {
        this.name = name;
        System.out.println("Animal constructor: " + name);
    }

    public void speak() {
        System.out.println(name + " makes a sound");
    }

    public Animal reproduce() {
        System.out.println(name + " reproduces");
        return new Animal("offspring");
    }
}

// ============================================================
// Derived class — extends, super(), @Override, covariant return
// ============================================================

class Dog extends Animal {
    public Dog(String name) {
        super(name);            // must be first statement
        System.out.println("Dog constructor: " + name);
    }

    @Override
    public void speak() {
        System.out.println(name + " says Woof!");
    }

    // Covariant return type — Dog overrides Animal.reproduce()
    @Override
    public Dog reproduce() {
        System.out.println(name + " has puppies");
        return new Dog("puppy");
    }

    public void fetch() {
        System.out.println(name + " fetches the ball");
    }
}

class Cat extends Animal {
    public Cat(String name) {
        super(name);
    }

    @Override
    public void speak() {
        System.out.println(name + " says Meow!");
    }
}

// ============================================================
// instanceof and casting (upcasting / downcasting)
// ============================================================

class CastingDemo {
    public static void main(String[] args) {
        Animal a = new Dog("Rex");          // upcast — implicit, safe

        if (a instanceof Dog) {
            Dog d = (Dog) a;                // downcast — explicit, checked
            d.fetch();
        }

        // Pattern matching for instanceof (Java 16+)
        if (a instanceof Dog d) {
            d.fetch();
        }

        // Does not compile: Cat c = (Cat) a;  // ClassCastException at runtime
    }
}

// ============================================================
// Constructor chaining — parent constructors run in chain
// ============================================================

class GrandParent {
    GrandParent() { System.out.println("GrandParent"); }
}

class Parent extends GrandParent {
    Parent() { System.out.println("Parent"); }
}

class Child extends Parent {
    Child() { System.out.println("Child"); }
}

// ============================================================
// Composition over inheritance
// ============================================================

// Instead of: class Car extends Engine  (IS-A, wrong)
// Prefer:     class Car has-an Engine   (HAS-A, right)

class Engine {
    void start() { System.out.println("Engine starts"); }
}

class Car {                    // composition
    private Engine engine;

    Car() {
        this.engine = new Engine();
    }

    void start() {
        engine.start();
    }
}

// ============================================================
// Main
// ============================================================

public class InheritanceDemo {
    public static void main(String[] args) {
        System.out.println("--- Constructor chaining ---");
        Child c = new Child();   // prints GrandParent, Parent, Child

        System.out.println("\n--- Polymorphism via inheritance ---");
        Animal[] animals = { new Dog("Rex"), new Cat("Whiskers") };

        for (Animal animal : animals) {
            animal.speak();              // dynamic dispatch
        }

        // Casting
        System.out.println("\n--- Downcasting ---");
        Animal a = new Dog("Buddy");
        if (a instanceof Dog d) {
            d.fetch();
        }
    }
}
```
