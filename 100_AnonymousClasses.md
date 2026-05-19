# Lesson 100: Anonymous Classes

## Key Concepts
- An anonymous class is a class without a name, defined and instantiated in a single expression
- Used to implement interfaces or extend abstract/concrete classes on the spot
- Often used for event handlers (`ActionListener`), threads (`Runnable`), and callbacks
- Anonymous classes can access `final` or effectively-final local variables
- Syntax: `new InterfaceOrClass() { /* overrides */ }`

## Code Example

```java
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Anonymous Classes Demo ===\n");

        Greeting englishGreeting = new Greeting() {
            @Override
            public void greet() {
                System.out.println("Hello!");
            }
        };

        Greeting spanishGreeting = new Greeting() {
            @Override
            public void greet() {
                System.out.println("Hola!");
            }
        };

        Greeting frenchGreeting = new Greeting() {
            @Override
            public void greet() {
                System.out.println("Bonjour!");
            }
        };

        englishGreeting.greet();
        spanishGreeting.greet();
        frenchGreeting.greet();

        System.out.println("\n--- Button with anonymous class ---");
        JButton button = new JButton("Click me");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Button clicked!");
            }
        });

        System.out.println("--- Thread with anonymous class ---");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Running in a thread!");
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Animal dog = new Animal() {
            @Override
            void speak() {
                System.out.println("Woof! (from anonymous class)");
            }
        };
        dog.speak();
    }
}

interface Greeting {
    void greet();
}

abstract class Animal {
    abstract void speak();
}
```

## Explanation
1. **Interface anonymous class**: `Greeting` is an interface with one method. Three different anonymous classes implement it, each printing a different greeting.
2. **ActionListener**: Before lambdas, anonymous classes were the standard way to handle Swing events. `new ActionListener() { ... }` creates an implementation inline.
3. **Runnable**: An anonymous `Runnable` is passed to a `Thread` constructor. Its `run()` method defines the work.
4. **Abstract class**: `Animal` is an abstract class with `speak()`. An anonymous subclass provides the implementation.
5. Anonymous classes are best suited for simple, one-use implementations. For more complex reuse, define a named class.

## Expected Output

```
=== Anonymous Classes Demo ===

Hello!
Hola!
Bonjour!

--- Button with anonymous class ---
--- Thread with anonymous class ---
Running in a thread!
Woof! (from anonymous class)
```

*(The "Button clicked!" message would appear when the button is pressed in a GUI.)*
