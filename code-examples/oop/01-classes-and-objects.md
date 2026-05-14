# Classes and Objects

Classes are blueprints for objects. An object is an instance of a class with state (fields) and behavior (methods). Constructors initialize new objects. The `this` keyword disambiguates field shadowing and enables constructor chaining. Java passes object references by value — the reference itself is copied, but both the original and the copy point to the same heap object.

```java
// ============================================================
// 1. Class definition with fields, constructors
// ============================================================

class Person {
    private String name;
    private int age;

    // No-arg constructor
    public Person() {
        this("Unknown", 0);   // constructor chaining via this()
    }

    // Parameterized constructor
    public Person(String name, int age) {
        this.name = name;     // this.name = field, name = parameter
        this.age = age;
    }

    // Copy constructor
    public Person(Person other) {
        this(other.name, other.age);
    }

    // Getters (see encapsulation.md for detail)
    public String getName() { return name; }
    public int getAge() { return age; }
}

// ============================================================
// 2. Multiple classes interacting
// ============================================================

class Address {
    String street;
    String city;

    Address(String street, String city) {
        this.street = street;
        this.city = city;
    }
}

class Employee {
    private String name;
    private Address address;   // object reference field

    public Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public void display() {
        System.out.println(name + " lives at " + address.street + ", " + address.city);
    }
}

// ============================================================
// 3. Object references vs primitives when passed to methods
// ============================================================

class ReferenceDemo {
    // Primitives are passed by value — original unaffected
    static void modifyPrimitive(int x) {
        x = 99;
    }

    // Object references are passed by value too,
    // but the reference still points to the same object
    static void modifyObject(Person p) {
        p = new Person("New", 1);   // reassignment lost after return
    }

    static void mutateObject(Person p) {
        // We can mutate the object the reference points to
        // But since fields are private and there are no setters,
        // we'd need a mutable class — see encapsulation.md
    }
}

// ============================================================
// 4. Main — putting it all together
// ============================================================

public class ClassesAndObjectsDemo {
    public static void main(String[] args) {
        // Using different constructors
        Person p1 = new Person();                              // no-arg
        Person p2 = new Person("Alice", 30);                   // parameterized
        Person p3 = new Person(p2);                            // copy constructor

        System.out.println(p1.getName() + " " + p1.getAge());  // Unknown 0
        System.out.println(p2.getName() + " " + p2.getAge());  // Alice 30
        System.out.println(p3.getName() + " " + p3.getAge());  // Alice 30

        // Multiple classes interacting
        Address addr = new Address("123 Main St", "Springfield");
        Employee emp = new Employee("Bob", addr);
        emp.display();                                         // Bob lives at 123 Main St, Springfield

        // Primitive vs reference semantics
        int val = 10;
        ReferenceDemo.modifyPrimitive(val);
        System.out.println(val);                               // 10 (unchanged)

        Person original = new Person("Charlie", 40);
        ReferenceDemo.modifyObject(original);
        System.out.println(original.getName());                // Charlie (reassignment was lost)
    }
}
```
