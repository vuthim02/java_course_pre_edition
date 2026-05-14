# Design Patterns in Java

> Complete, compilable Java examples for 27 essential design patterns.
> All examples include a `main()` method and are ready to run.

---

## Table of Contents

1. [Creational Patterns](#creational-patterns)
   - [Singleton](#1-singleton)
   - [Factory Method](#2-factory-method)
   - [Abstract Factory](#3-abstract-factory)
   - [Builder](#4-builder)
   - [Prototype](#5-prototype)
2. [Structural Patterns](#structural-patterns)
   - [Adapter](#6-adapter)
   - [Bridge](#7-bridge)
   - [Composite](#8-composite)
   - [Decorator](#9-decorator)
   - [Facade](#10-facade)
   - [Flyweight](#11-flyweight)
   - [Proxy](#12-proxy)
3. [Behavioral Patterns](#behavioral-patterns)
   - [Chain of Responsibility](#13-chain-of-responsibility)
   - [Command](#14-command)
   - [Interpreter](#15-interpreter)
   - [Iterator](#16-iterator)
   - [Mediator](#17-mediator)
   - [Memento](#18-memento)
   - [Observer](#19-observer)
   - [State](#20-state)
   - [Strategy](#21-strategy)
   - [Template Method](#22-template-method)
4. [Enterprise/Modern Patterns](#enterprisemodern-patterns)
   - [DAO/Repository](#23-daorepository)
   - [Dependency Injection](#24-dependency-injection)
   - [Null Object](#25-null-object)
   - [Specification](#26-specification)
   - [Pipeline/Fluent Builder](#27-pipelinefluent-builder)

---

# Creational Patterns

---

## 1. Singleton

**Purpose:** Ensure a class has exactly one instance and provide a global access point.

**Use case:** Database connections, logging, configuration managers, thread pools.

### Variant A: Double-Checked Locking (Thread-safe, Lazy)

```java
/*
 *  +-----------+
 *  | Singleton |
 *  +-----------+
 *  | -instance  |
 *  +-----------+
 *  | +get()    |
 *  +-----------+
 */

public class SingletonDCL {
    // volatile ensures visibility across threads
    private static volatile SingletonDCL instance;

    private SingletonDCL() {
        // prevent reflection
        if (instance != null) {
            throw new RuntimeException("Use getInstance()");
        }
    }

    public static SingletonDCL getInstance() {
        SingletonDCL local = instance;
        if (local == null) {
            synchronized (SingletonDCL.class) {
                local = instance;
                if (local == null) {
                    instance = local = new SingletonDCL();
                }
            }
        }
        return local;
    }

    public void show() {
        System.out.println("SingletonDCL instance: " + hashCode());
    }

    public static void main(String[] args) {
        SingletonDCL s1 = SingletonDCL.getInstance();
        SingletonDCL s2 = SingletonDCL.getInstance();
        System.out.println("Same instance? " + (s1 == s2)); // true
        s1.show();
    }
}
```

### Variant B: Bill Pugh Inner Class (Thread-safe, Lazy, Fast)

```java
public class SingletonBillPugh {

    private SingletonBillPugh() {}

    // inner class is loaded only on first access
    private static class Holder {
        static final SingletonBillPugh INSTANCE = new SingletonBillPugh();
    }

    public static SingletonBillPugh getInstance() {
        return Holder.INSTANCE;
    }

    public void show() {
        System.out.println("SingletonBillPugh instance: " + hashCode());
    }

    public static void main(String[] args) {
        SingletonBillPugh s1 = SingletonBillPugh.getInstance();
        SingletonBillPugh s2 = SingletonBillPugh.getInstance();
        System.out.println("Same instance? " + (s1 == s2));
        s1.show();
    }
}
```

### Variant C: Enum Singleton (Immutable, Serialization-safe)

```java
/*
 * Simplest and safest singleton in Java.
 * Enum constructors are guaranteed to be invoked only once
 * by the JVM, and enums provide built-in serialization safety.
 */
public enum SingletonEnum {
    INSTANCE;

    private String config = "default";

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public void show() {
        System.out.println("SingletonEnum: " + config);
    }

    public static void main(String[] args) {
        SingletonEnum.INSTANCE.setConfig("production");
        SingletonEnum.INSTANCE.show();
        System.out.println("Same? " + (SingletonEnum.INSTANCE == SingletonEnum.INSTANCE));
    }
}
```

---

## 2. Factory Method

**Purpose:** Define an interface for creating an object, but let subclasses decide which class to instantiate.

**Use case:** Logging frameworks, document generators, payment gateways.

```java
/*
 *  +------------+       +------------------+
 *  | Creator    |------>| Product  (interface) |
 *  +------------+       +------------------+
 *  | +create()  |       | +operation()     |
 *  | +doWork()  |       +------------------+
 *  +------------+             /\
 *       /\                   /  \
 *  +-----------+      +--------+  +--------+
 *  |Concrete   |      |ProductA|  |ProductB|
 *  |CreatorA/B |      +--------+  +--------+
 *  +-----------+
 */

// Product interface
interface Transport {
    void deliver();
}

// Concrete products
class Truck implements Transport {
    @Override public void deliver() {
        System.out.println("Delivering by land in a truck.");
    }
}

class Ship implements Transport {
    @Override public void deliver() {
        System.out.println("Delivering by sea in a ship.");
    }
}

// Creator (abstract)
abstract class Logistics {
    // factory method
    public abstract Transport createTransport();

    public void planDelivery() {
        Transport t = createTransport();
        t.deliver();
    }
}

// Concrete creators
class RoadLogistics extends Logistics {
    @Override public Transport createTransport() {
        return new Truck();
    }
}

class SeaLogistics extends Logistics {
    @Override public Transport createTransport() {
        return new Ship();
    }
}

public class FactoryMethodDemo {
    public static void main(String[] args) {
        Logistics logistics;

        String type = args.length > 0 ? args[0] : "road";
        if (type.equals("sea")) {
            logistics = new SeaLogistics();
        } else {
            logistics = new RoadLogistics();
        }

        logistics.planDelivery();
    }
}
```

---

## 3. Abstract Factory

**Purpose:** Provide an interface for creating families of related or dependent objects without specifying their concrete classes.

**Use case:** UI toolkits (different OS themes), database drivers, furniture store.

```java
/*
 *  +------------------+       +-------------------+
 *  | AbstractFactory  |------>| AbstractProductA   |
 *  +------------------+       +-------------------+
 *  | +createChair()   |
 *  | +createTable()   |
 *  +------------------+
 *       /\        /\
 *      /          \
 *  +--------+   +--------+
 *  |Victorian|   |Modern  |
 *  | Factory |   | Factory|
 *  +--------+   +--------+
 */

// Abstract products
interface Chair {
    void sitOn();
}

interface Table {
    void placeItem();
}

// Concrete products
class VictorianChair implements Chair {
    @Override public void sitOn() { System.out.println("Sitting on Victorian chair."); }
}

class ModernChair implements Chair {
    @Override public void sitOn() { System.out.println("Sitting on modern chair."); }
}

class VictorianTable implements Table {
    @Override public void placeItem() { System.out.println("Placing on Victorian table."); }
}

class ModernTable implements Table {
    @Override public void placeItem() { System.out.println("Placing on modern table."); }
}

// Abstract factory
interface FurnitureFactory {
    Chair createChair();
    Table createTable();
}

// Concrete factories
class VictorianFactory implements FurnitureFactory {
    @Override public Chair createChair() { return new VictorianChair(); }
    @Override public Table createTable() { return new VictorianTable(); }
}

class ModernFactory implements FurnitureFactory {
    @Override public Chair createChair() { return new ModernChair(); }
    @Override public Table createTable() { return new ModernTable(); }
}

public class AbstractFactoryDemo {
    public static void main(String[] args) {
        FurnitureFactory factory;

        String style = args.length > 0 ? args[0] : "modern";
        if (style.equals("victorian")) {
            factory = new VictorianFactory();
        } else {
            factory = new ModernFactory();
        }

        Chair chair = factory.createChair();
        Table table = factory.createTable();
        chair.sitOn();
        table.placeItem();
    }
}
```

---

## 4. Builder

**Purpose:** Separate the construction of a complex object from its representation.

**Use case:** Objects with many optional parameters (e.g., SQL queries, HTTP requests, meal orders).

### Classic Builder with Director

```java
/*
 *  +----------+     +-----------+
 *  | Director |---->| Builder   |<interface>
 *  +----------+     +-----------+
 *  | +construct|    | +buildPart|
 *  +----------+     +-----------+
 *                          /\
 *                    +-----------+
 *                    |Concrete   |
 *                    | Builder   |
 *                    +-----------+
 *                         |
 *                    +-----------+
 *                    | Product   |
 *                    +-----------+
 */

class Pizza {
    private String dough;
    private String sauce;
    private String topping;

    public void setDough(String dough) { this.dough = dough; }
    public void setSauce(String sauce) { this.sauce = sauce; }
    public void setTopping(String topping) { this.topping = topping; }

    @Override public String toString() {
        return "Pizza{" + dough + ", " + sauce + ", " + topping + "}";
    }
}

interface PizzaBuilder {
    void buildDough();
    void buildSauce();
    void buildTopping();
    Pizza getPizza();
}

class HawaiianPizzaBuilder implements PizzaBuilder {
    private Pizza pizza = new Pizza();

    @Override public void buildDough()   { pizza.setDough("thin crust"); }
    @Override public void buildSauce()   { pizza.setSauce("tomato"); }
    @Override public void buildTopping() { pizza.setTopping("ham + pineapple"); }
    @Override public Pizza getPizza()    { return pizza; }
}

class SpicyPizzaBuilder implements PizzaBuilder {
    private Pizza pizza = new Pizza();

    @Override public void buildDough()   { pizza.setDough("pan baked"); }
    @Override public void buildSauce()   { pizza.setSauce("hot chili"); }
    @Override public void buildTopping() { pizza.setTopping("pepperoni + jalapeno"); }
    @Override public Pizza getPizza()    { return pizza; }
}

// Director orchestrates the building sequence
class PizzaDirector {
    private PizzaBuilder builder;

    public PizzaDirector(PizzaBuilder builder) { this.builder = builder; }

    public void construct() {
        builder.buildDough();
        builder.buildSauce();
        builder.buildTopping();
    }
}

public class BuilderDemo {
    public static void main(String[] args) {
        PizzaBuilder builder = new HawaiianPizzaBuilder();
        PizzaDirector director = new PizzaDirector(builder);
        director.construct();
        System.out.println(builder.getPizza());

        builder = new SpicyPizzaBuilder();
        director = new PizzaDirector(builder);
        director.construct();
        System.out.println(builder.getPizza());
    }
}
```

### Fluent Builder (Lombok @Builder style)

```java
class Email {
    private final String to;
    private final String from;
    private final String subject;
    private final String body;
    private final boolean isHtml;

    private Email(Builder b) {
        this.to = b.to;
        this.from = b.from;
        this.subject = b.subject;
        this.body = b.body;
        this.isHtml = b.isHtml;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String from;
        private String subject;
        private String body;
        private boolean isHtml;

        public Builder to(String to)       { this.to = to; return this; }
        public Builder from(String from)   { this.from = from; return this; }
        public Builder subject(String sub) { this.subject = sub; return this; }
        public Builder body(String body)   { this.body = body; return this; }
        public Builder html(boolean html)  { this.isHtml = html; return this; }
        public Email build()               { return new Email(this); }
    }

    @Override public String toString() {
        return "Email{to='" + to + "', from='" + from + "', subject='" + subject + "'}";
    }

    public static void main(String[] args) {
        Email email = Email.builder()
            .to("user@example.com")
            .from("noreply@example.com")
            .subject("Welcome!")
            .body("Thanks for joining.")
            .html(false)
            .build();
        System.out.println(email);
    }
}
```

---

## 5. Prototype

**Purpose:** Create new objects by copying an existing object (a prototype).

**Use case:** Objects that are expensive to create (e.g., loading data from DB), avoiding subclass explosion.

```java
/*
 *  +-----------+
 *  | Prototype |<interface>
 *  +-----------+
 *  | +clone()  |
 *  +-----------+
 *       /\
 *       ||
 *  +-----------+
 *  |Concrete   |
 *  | Prototype |
 *  +-----------+
 */

abstract class Shape implements Cloneable {
    protected String id;
    protected String color;

    public void setColor(String color) { this.color = color; }

    @Override
    public Shape clone() {
        try {
            return (Shape) super.clone(); // shallow copy
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void draw();
}

class Circle extends Shape {
    private int radius;

    public Circle(int radius, String color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void draw() {
        System.out.println("Circle(color=" + color + ", radius=" + radius + ")");
    }

    // To demonstrate deep copy, override clone for mutable fields
    @Override
    public Circle clone() {
        return (Circle) super.clone(); // shallow is fine here (primitives + String)
    }
}

class Rectangle extends Shape {
    private int width;
    private int height;

    public Rectangle(int width, int height, String color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    @Override
    public void draw() {
        System.out.println("Rectangle(color=" + color + ", " + width + "x" + height + ")");
    }

    @Override
    public Rectangle clone() {
        return (Rectangle) super.clone();
    }
}

public class PrototypeDemo {
    public static void main(String[] args) {
        Circle circlePrototype = new Circle(10, "red");
        Rectangle rectPrototype = new Rectangle(20, 30, "blue");

        // Clone and modify
        Circle c1 = circlePrototype.clone();
        c1.setColor("green");

        Rectangle r1 = rectPrototype.clone();
        r1.draw(); // blue 20x30
        c1.draw(); // green radius=10

        // Deep copy example with a mutable field
        ShapeRegistry registry = new ShapeRegistry();
        registry.addItem("bigred", new Circle(50, "red"));
        Shape cloned = registry.getClone("bigred");
        cloned.setColor("yellow");
        cloned.draw(); // yellow radius=50
    }
}

// Prototype registry
class ShapeRegistry {
    private java.util.Map<String, Shape> items = new java.util.HashMap<>();

    public void addItem(String key, Shape s) { items.put(key, s); }
    public Shape getClone(String key)        { return items.get(key).clone(); }
}
```

---

# Structural Patterns

---

## 6. Adapter

**Purpose:** Allow incompatible interfaces to work together.

**Use case:** Legacy code integration, third-party library wrappers, different data formats.

### Object Adapter (Composition)

```java
/*
 *  +--------+      +----------+      +----------+
 *  | Client |----->| Target   |<-----| Adapter  |
 *  +--------+      | (iface)  |      +----------+
 *                   +----------+      | -adaptee |
 *                          0          +----------+
 *                          1               |
 *                      +----------+         |
 *                      | Adaptee  |<--------+
 *                      +----------+
 *                      | +specific|
 *                      +----------+
 */

// Target interface the client expects
interface MediaPlayer {
    void play(String audioType, String fileName);
}

// Adaptee (incompatible interface)
class AdvancedMediaPlayer {
    public void playVlc(String file) {
        System.out.println("Playing VLC: " + file);
    }

    public void playMp4(String file) {
        System.out.println("Playing MP4: " + file);
    }
}

// Adapter wraps Adaptee and implements Target
class MediaAdapter implements MediaPlayer {
    private AdvancedMediaPlayer advanced = new AdvancedMediaPlayer();

    @Override
    public void play(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("vlc")) {
            advanced.playVlc(fileName);
        } else if (audioType.equalsIgnoreCase("mp4")) {
            advanced.playMp4(fileName);
        }
    }
}

// Client
class AudioPlayer implements MediaPlayer {
    private MediaAdapter adapter = new MediaAdapter();

    @Override
    public void play(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("mp3")) {
            System.out.println("Playing MP3: " + fileName);
        } else {
            adapter.play(audioType, fileName);
        }
    }
}

public class AdapterDemo {
    public static void main(String[] args) {
        AudioPlayer player = new AudioPlayer();
        player.play("mp3", "song.mp3");
        player.play("mp4", "video.mp4");
        player.play("vlc", "movie.vlc");
        player.play("avi", "unsupported.avi"); // ignored
    }
}
```

---

## 7. Bridge

**Purpose:** Decouple an abstraction from its implementation so they can vary independently.

**Use case:** Device/remote control, rendering engines, database drivers (JDBC).

```java
/*
 *  +----------+     +------------+
 *  |Abstraction|---->|Implementor |
 *  +----------+     +------------+
 *  | +operation|     | +operationImpl|
 *  +----------+     +------------+
 *       /\               /\
 *   +-------+       +--------+ +--------+
 *   |Refined |       |Concrete| |Concrete|
 *   |Abstrac.|       |Impl A  | |Impl B  |
 *   +-------+       +--------+ +--------+
 */

// Implementor
interface Device {
    boolean isEnabled();
    void enable();
    void disable();
    int getVolume();
    void setVolume(int vol);
}

// Concrete Implementors
class TV implements Device {
    private boolean on = false;
    private int volume = 30;

    @Override public boolean isEnabled() { return on; }
    @Override public void enable()       { on = true; System.out.println("TV ON"); }
    @Override public void disable()      { on = false; System.out.println("TV OFF"); }
    @Override public int getVolume()     { return volume; }
    @Override public void setVolume(int v) { volume = v; System.out.println("TV volume: " + volume); }
}

class Radio implements Device {
    private boolean on = false;
    private int volume = 10;

    @Override public boolean isEnabled() { return on; }
    @Override public void enable()       { on = true; System.out.println("Radio ON"); }
    @Override public void disable()      { on = false; System.out.println("Radio OFF"); }
    @Override public int getVolume()     { return volume; }
    @Override public void setVolume(int v) { volume = v; System.out.println("Radio volume: " + volume); }
}

// Abstraction
abstract class Remote {
    protected Device device;

    public Remote(Device device) { this.device = device; }

    public abstract void togglePower();
    public abstract void volumeUp();
    public abstract void volumeDown();
}

// Refined Abstraction
class BasicRemote extends Remote {
    public BasicRemote(Device device) { super(device); }

    @Override public void togglePower() {
        if (device.isEnabled()) device.disable();
        else device.enable();
    }

    @Override public void volumeUp()   { device.setVolume(device.getVolume() + 5); }
    @Override public void volumeDown() { device.setVolume(device.getVolume() - 5); }
}

public class BridgeDemo {
    public static void main(String[] args) {
        Device tv = new TV();
        Remote remote = new BasicRemote(tv);
        remote.togglePower();
        remote.volumeUp();

        Device radio = new Radio();
        remote = new BasicRemote(radio);
        remote.togglePower();
        remote.volumeUp();
    }
}
```

---

## 8. Composite

**Purpose:** Compose objects into tree structures to represent part-whole hierarchies.

**Use case:** File systems, GUI component trees, organizational charts.

```java
/*
 *  +-----------+
 *  | Component |<interface>
 *  +-----------+
 *  | +operation|
 *  | +add/remove|
 *  +-----------+
 *       /\              /\
 *      /                \
 *  +--------+      +----------+
 *  | Leaf   |      | Composite|
 *  +--------+      +----------+
 *                   | -children|
 *                   +----------+
 */

import java.util.ArrayList;
import java.util.List;

// Component
interface FileSystemComponent {
    void showDetails(String indent);
    int getSize();
}

// Leaf
class File implements FileSystemComponent {
    private String name;
    private int size; // in KB

    public File(String name, int size) { this.name = name; this.size = size; }

    @Override public void showDetails(String indent) {
        System.out.println(indent + "File: " + name + " (" + size + " KB)");
    }

    @Override public int getSize() { return size; }
}

// Composite
class Directory implements FileSystemComponent {
    private String name;
    private List<FileSystemComponent> children = new ArrayList<>();

    public Directory(String name) { this.name = name; }

    public void add(FileSystemComponent c) { children.add(c); }
    public void remove(FileSystemComponent c) { children.remove(c); }

    @Override public void showDetails(String indent) {
        System.out.println(indent + "Directory: " + name);
        for (FileSystemComponent c : children) {
            c.showDetails(indent + "  ");
        }
    }

    @Override public int getSize() {
        return children.stream().mapToInt(FileSystemComponent::getSize).sum();
    }
}

public class CompositeDemo {
    public static void main(String[] args) {
        Directory root = new Directory("root");
        Directory home = new Directory("home");
        Directory user = new Directory("user");

        File f1 = new File("readme.txt", 5);
        File f2 = new File("photo.jpg", 200);
        File f3 = new File("data.csv", 15);

        user.add(f1);
        user.add(f2);
        home.add(user);
        home.add(f3);
        root.add(home);

        root.showDetails("");
        System.out.println("Total size: " + root.getSize() + " KB");
    }
}
```

---

## 9. Decorator

**Purpose:** Attach additional responsibilities to an object dynamically.

**Use case:** Java I/O streams (BufferedReader wraps FileReader), coffee shop pricing, middleware pipelines.

```java
/*
 *  +-----------+
 *  | Component |<interface>
 *  +-----------+
 *  | +cost()   |
 *  +-----------+
 *       /\               /\
 *      /                 \
 *  +--------+      +------------+
 *  |Concrete|      | Decorator  |
 *  |Component|     +------------+
 *  +--------+      | -component |
 *                   | +cost()   |
 *                   +------------+
 *                        /\
 *                   +--------+  +--------+
 *                   |Concrete|  |Concrete|
 *                   |DecorA  |  |DecorB  |
 *                   +--------+  +--------+
 */

// Component
interface Beverage {
    String getDescription();
    double cost();
}

// Concrete component
class Espresso implements Beverage {
    @Override public String getDescription() { return "Espresso"; }
    @Override public double cost() { return 1.50; }
}

// Decorator
abstract class CondimentDecorator implements Beverage {
    protected Beverage beverage;

    public CondimentDecorator(Beverage beverage) { this.beverage = beverage; }

    @Override public String getDescription() {
        return beverage.getDescription();
    }

    @Override public double cost() {
        return beverage.cost();
    }
}

// Concrete decorators
class Milk extends CondimentDecorator {
    public Milk(Beverage beverage) { super(beverage); }

    @Override public String getDescription() {
        return beverage.getDescription() + ", Milk";
    }

    @Override public double cost() {
        return beverage.cost() + 0.50;
    }
}

class Sugar extends CondimentDecorator {
    public Sugar(Beverage beverage) { super(beverage); }

    @Override public String getDescription() {
        return beverage.getDescription() + ", Sugar";
    }

    @Override public double cost() {
        return beverage.cost() + 0.25;
    }
}

class WhippedCream extends CondimentDecorator {
    public WhippedCream(Beverage beverage) { super(beverage); }

    @Override public String getDescription() {
        return beverage.getDescription() + ", Whipped Cream";
    }

    @Override public double cost() {
        return beverage.cost() + 0.75;
    }
}

public class DecoratorDemo {
    public static void main(String[] args) {
        Beverage espresso = new Espresso();
        System.out.println(espresso.getDescription() + " = $" + espresso.cost());

        Beverage custom = new WhippedCream(new Sugar(new Milk(new Espresso())));
        System.out.println(custom.getDescription() + " = $" + custom.cost());
    }
}
```

---

## 10. Facade

**Purpose:** Provide a unified interface to a set of interfaces in a subsystem.

**Use case:** Complex libraries (video conversion, home theater), API wrappers.

```java
/*
 *  +--------+
 *  | Facade |------> Subsystem classes
 *  +--------+
 *  | +simple|
 *  +--------+
 */

// Complex subsystem classes
class Amplifier {
    public void on()  { System.out.println("Amplifier ON"); }
    public void off() { System.out.println("Amplifier OFF"); }
}

class DVDPlayer {
    public void on()                { System.out.println("DVD ON"); }
    public void play(String movie)  { System.out.println("Playing: " + movie); }
    public void off()               { System.out.println("DVD OFF"); }
}

class Projector {
    public void on()   { System.out.println("Projector ON"); }
    public void off()  { System.out.println("Projector OFF"); }
}

class Screen {
    public void down() { System.out.println("Screen DOWN"); }
    public void up()   { System.out.println("Screen UP"); }
}

// Facade
class HomeTheaterFacade {
    private Amplifier amp;
    private DVDPlayer dvd;
    private Projector projector;
    private Screen screen;

    public HomeTheaterFacade() {
        this.amp = new Amplifier();
        this.dvd = new DVDPlayer();
        this.projector = new Projector();
        this.screen = new Screen();
    }

    public void watchMovie(String movie) {
        System.out.println("--- Starting movie ---");
        screen.down();
        projector.on();
        amp.on();
        dvd.on();
        dvd.play(movie);
    }

    public void endMovie() {
        System.out.println("--- Ending movie ---");
        dvd.off();
        amp.off();
        projector.off();
        screen.up();
    }
}

public class FacadeDemo {
    public static void main(String[] args) {
        HomeTheaterFacade home = new HomeTheaterFacade();
        home.watchMovie("Inception");
        home.endMovie();
    }
}
```

---

## 11. Flyweight

**Purpose:** Share fine-grained objects to minimize memory usage.

**Use case:** Text editors (character glyphs), game particle systems, GUI widgets.

```java
/*
 *   +-----------+
 *   | Flyweight |<interface>
 *   +-----------+
 *   | +operation|   (intrinsic state inside)
 *   +-----------+
 *        /\
 *   +-----------+      +------------+
 *   |Concrete   |<-----| Flyweight  |
 *   | Flyweight |      | Factory    |
 *   +-----------+      +------------+
 *   | -color    |      | +get(key)  |
 *   | -shape    |      +------------+
 *   +-----------+
 */

import java.util.HashMap;
import java.util.Map;

// Flyweight (intrinsic state is shared)
class TreeType {
    private String name;
    private String color;
    private String texture;

    public TreeType(String name, String color, String texture) {
        this.name = name;
        this.color = color;
        this.texture = texture;
    }

    // extrinsic state passed in
    public void display(int x, int y) {
        System.out.println(name + " tree at (" + x + "," + y + ")");
    }
}

// Flyweight Factory
class TreeFactory {
    private static Map<String, TreeType> types = new HashMap<>();

    public static TreeType getTreeType(String name, String color, String texture) {
        String key = name + "|" + color + "|" + texture;
        types.putIfAbsent(key, new TreeType(name, color, texture));
        return types.get(key);
    }

    public static int size() { return types.size(); }
}

// Context (extrinsic state)
class Tree {
    private int x, y;
    private TreeType type;

    public Tree(int x, int y, TreeType type) {
        this.x = x; this.y = y; this.type = type;
    }

    public void display() { type.display(x, y); }
}

public class FlyweightDemo {
    public static void main(String[] args) {
        Tree[] forest = new Tree[10];
        for (int i = 0; i < 5; i++) {
            forest[i] = new Tree(i * 10, i * 10,
                TreeFactory.getTreeType("Oak", "Green", "Rough"));
        }
        for (int i = 5; i < 10; i++) {
            forest[i] = new Tree(i * 10, i * 10,
                TreeFactory.getTreeType("Pine", "Dark Green", "Smooth"));
        }
        for (Tree t : forest) t.display();
        System.out.println("Unique tree types: " + TreeFactory.size());
    }
}
```

---

## 12. Proxy

**Purpose:** Provide a surrogate or placeholder for another object to control access.

**Use case:** Lazy loading, access control, logging, remote objects (RMI).

```java
/*
 *  +----------+
 *  | Subject  |<interface>
 *  +----------+
 *  | +request |
 *  +----------+
 *       /\          /\
 *      /            \
 *  +--------+   +---------+
 *  | Real   |   | Proxy   |
 *  |Subject |   +---------+
 *  +--------+   | -real   |
 *               +---------+
 */

// Subject
interface Image {
    void display();
}

// Real Subject
class HighResImage implements Image {
    private String filename;

    public HighResImage(String filename) {
        this.filename = filename;
        loadFromDisk(); // expensive operation
    }

    private void loadFromDisk() {
        System.out.println("Loading " + filename + " from disk...");
        try { Thread.sleep(500); } catch (InterruptedException e) { }
    }

    @Override public void display() {
        System.out.println("Displaying " + filename);
    }
}

// Virtual Proxy (lazy loading)
class ImageProxy implements Image {
    private String filename;
    private HighResImage real;

    public ImageProxy(String filename) { this.filename = filename; }

    @Override public void display() {
        if (real == null) real = new HighResImage(filename); // lazy init
        real.display();
    }
}

// Protection Proxy (access control)
class ProtectedImageProxy implements Image {
    private Image real;
    private boolean authorized;

    public ProtectedImageProxy(Image real, boolean authorized) {
        this.real = real;
        this.authorized = authorized;
    }

    @Override public void display() {
        if (authorized) {
            real.display();
        } else {
            System.out.println("Access denied to image.");
        }
    }
}

public class ProxyDemo {
    public static void main(String[] args) {
        System.out.println("=== Virtual Proxy (lazy loading) ===");
        Image img1 = new ImageProxy("photo1.jpg");
        Image img2 = new ImageProxy("photo2.jpg");
        // Image is loaded only when displayed
        img1.display();
        img1.display(); // cached, no reload

        System.out.println("\n=== Protection Proxy ===");
        Image real = new HighResImage("secret.png");
        Image authorized = new ProtectedImageProxy(real, true);
        Image unauthorized = new ProtectedImageProxy(real, false);
        authorized.display();
        unauthorized.display();
    }
}
```

---

# Behavioral Patterns

---

## 13. Chain of Responsibility

**Purpose:** Pass a request along a chain of handlers until one handles it.

**Use case:** Logging frameworks, event handling, middleware pipelines.

```java
/*
 *  +----------+      +----------+      +----------+
 *  | Handler  |----->| Handler  |----->| Handler  |
 *  +----------+      +----------+      +----------+
 *  | #handle() |      | #handle() |      | #handle() |
 *  +----------+      +----------+      +----------+
 */

abstract class Logger {
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int ERROR = 3;

    protected int level;
    protected Logger next;

    public Logger setNext(Logger next) {
        this.next = next;
        return next;
    }

    public void log(int level, String message) {
        if (this.level <= level) {
            write(message);
        }
        if (next != null) {
            next.log(level, message);
        }
    }

    protected abstract void write(String message);
}

class ConsoleLogger extends Logger {
    public ConsoleLogger(int level) { this.level = level; }

    @Override protected void write(String msg) {
        System.out.println("[Console] " + msg);
    }
}

class FileLogger extends Logger {
    public FileLogger(int level) { this.level = level; }

    @Override protected void write(String msg) {
        System.out.println("[File] " + msg);
    }
}

class EmailLogger extends Logger {
    public EmailLogger(int level) { this.level = level; }

    @Override protected void write(String msg) {
        System.out.println("[Email] " + msg);
    }
}

public class ChainOfResponsibilityDemo {
    public static void main(String[] args) {
        Logger chain = new ConsoleLogger(Logger.INFO);
        Logger file = new FileLogger(Logger.DEBUG);
        Logger email = new EmailLogger(Logger.ERROR);

        chain.setNext(file).setNext(email);

        System.out.println("--- INFO ---");
        chain.log(Logger.INFO, "Info message");
        System.out.println("--- DEBUG ---");
        chain.log(Logger.DEBUG, "Debug message");
        System.out.println("--- ERROR ---");
        chain.log(Logger.ERROR, "Error message");
    }
}
```

---

## 14. Command

**Purpose:** Encapsulate a request as an object, allowing parameterization, queuing, and undo.

**Use case:** Remote controls, transaction systems, undo/redo, task queues.

```java
/*
 *  +----------+      +-----------+
 *  | Command  |<-----| Invoker   |
 *  +----------+      +-----------+
 *  | +execute |      | +set/slot |
 *  | +undo    |      +-----------+
 *  +----------+
 *       /\
 *  +-----------+     +---------+
 *  |Concrete   |---->|Receiver |
 *  |Command    |     +---------+
 *  +-----------+     | +action |
 *                    +---------+
 */

// Receiver
class Light {
    private String location;

    public Light(String location) { this.location = location; }

    public void on()  { System.out.println(location + " light ON"); }
    public void off() { System.out.println(location + " light OFF"); }
}

// Command interface
interface Command {
    void execute();
    void undo();
}

// Concrete commands
class LightOnCommand implements Command {
    private Light light;

    public LightOnCommand(Light light) { this.light = light; }

    @Override public void execute() { light.on(); }
    @Override public void undo()    { light.off(); }
}

class LightOffCommand implements Command {
    private Light light;

    public LightOffCommand(Light light) { this.light = light; }

    @Override public void execute() { light.off(); }
    @Override public void undo()    { light.on(); }
}

// Invoker
class RemoteControl {
    private Command[] onSlots = new Command[2];
    private Command[] offSlots = new Command[2];
    private Command lastCommand;

    public RemoteControl() {
        for (int i = 0; i < 2; i++) {
            onSlots[i] = null;
            offSlots[i] = null;
        }
    }

    public void setCommand(int slot, Command on, Command off) {
        onSlots[slot] = on;
        offSlots[slot] = off;
    }

    public void pressOn(int slot) {
        if (onSlots[slot] != null) {
            onSlots[slot].execute();
            lastCommand = onSlots[slot];
        }
    }

    public void pressOff(int slot) {
        if (offSlots[slot] != null) {
            offSlots[slot].execute();
            lastCommand = offSlots[slot];
        }
    }

    public void pressUndo() {
        if (lastCommand != null) lastCommand.undo();
    }
}

public class CommandDemo {
    public static void main(String[] args) {
        Light livingRoom = new Light("Living Room");
        Light kitchen = new Light("Kitchen");

        RemoteControl remote = new RemoteControl();
        remote.setCommand(0, new LightOnCommand(livingRoom), new LightOffCommand(livingRoom));
        remote.setCommand(1, new LightOnCommand(kitchen), new LightOffCommand(kitchen));

        remote.pressOn(0);
        remote.pressOff(0);
        remote.pressUndo(); // undo off -> on

        remote.pressOn(1);
        remote.pressUndo(); // undo on -> off
    }
}
```

---

## 15. Interpreter

**Purpose:** Given a language, define a representation for its grammar and an interpreter.

**Use case:** SQL parsers, regex engines, expression evaluators, rule engines.

```java
/*
 *  +-----------+
 *  | Expression|<interface>
 *  +-----------+
 *  | +interpret|   (returns int)
 *  +-----------+
 *       /\
 *       |
 *  +-----------+     +----------+     +----------+
 *  |Number     |     |Plus      |     |Minus     |
 *  |Expression |     |Expression|     |Expression|
 *  +-----------+     +----------+     +----------+
 */

import java.util.Map;
import java.util.HashMap;

// Context: variable bindings
class Context {
    private Map<String, Integer> vars = new HashMap<>();

    public void set(String var, int val) { vars.put(var, val); }
    public int get(String var)           { return vars.getOrDefault(var, 0); }
}

// Abstract expression
interface Expression {
    int interpret(Context ctx);
}

// Terminal: number literal
class NumberExpr implements Expression {
    private int value;

    public NumberExpr(int value) { this.value = value; }

    @Override public int interpret(Context ctx) { return value; }
}

// Terminal: variable
class VariableExpr implements Expression {
    private String name;

    public VariableExpr(String name) { this.name = name; }

    @Override public int interpret(Context ctx) { return ctx.get(name); }
}

// Non-terminal: addition
class PlusExpr implements Expression {
    private Expression left, right;

    public PlusExpr(Expression left, Expression right) { this.left = left; this.right = right; }

    @Override public int interpret(Context ctx) {
        return left.interpret(ctx) + right.interpret(ctx);
    }
}

// Non-terminal: subtraction
class MinusExpr implements Expression {
    private Expression left, right;

    public MinusExpr(Expression left, Expression right) { this.left = left; this.right = right; }

    @Override public int interpret(Context ctx) {
        return left.interpret(ctx) - right.interpret(ctx);
    }
}

// Non-terminal: multiplication
class MultiplyExpr implements Expression {
    private Expression left, right;

    public MultiplyExpr(Expression left, Expression right) { this.left = left; this.right = right; }

    @Override public int interpret(Context ctx) {
        return left.interpret(ctx) * right.interpret(ctx);
    }
}

public class InterpreterDemo {
    public static void main(String[] args) {
        // Expression: (x + 5) * 2 - y
        Expression expr = new MinusExpr(
            new MultiplyExpr(
                new PlusExpr(new VariableExpr("x"), new NumberExpr(5)),
                new NumberExpr(2)
            ),
            new VariableExpr("y")
        );

        Context ctx = new Context();
        ctx.set("x", 10);
        ctx.set("y", 3);
        // (10 + 5) * 2 - 3 = 27
        System.out.println("Result: " + expr.interpret(ctx));
    }
}
```

---

## 16. Iterator

**Purpose:** Provide a way to access elements of a collection sequentially without exposing its internal structure.

**Use case:** Custom collection traversal, tree/in-order traversal.

```java
/*
 *  +-----------+       +------------+
 *  |Iterable   |------>| Iterator   |
 *  +-----------+       +------------+
 *  | +iterator |       | +hasNext   |
 *  +-----------+       | +next      |
 *                      +------------+
 */

import java.util.Iterator;
import java.util.NoSuchElementException;

// Custom collection
class CustomList<T> implements Iterable<T> {
    private Object[] items;
    private int size = 0;

    public CustomList(int capacity) { items = new Object[capacity]; }

    public void add(T item) {
        if (size < items.length) items[size++] = item;
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIterator();
    }

    // Inner class iterator
    private class ListIterator implements Iterator<T> {
        private int index = 0;

        @Override public boolean hasNext() { return index < size; }

        @Override @SuppressWarnings("unchecked")
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            return (T) items[index++];
        }
    }

    // Reverse iterator example
    public Iterator<T> reverseIterator() {
        return new Iterator<T>() {
            private int index = size - 1;

            @Override public boolean hasNext() { return index >= 0; }

            @Override @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return (T) items[index--];
            }
        };
    }
}

public class IteratorDemo {
    public static void main(String[] args) {
        CustomList<String> list = new CustomList<>(5);
        list.add("A");
        list.add("B");
        list.add("C");

        System.out.print("Forward: ");
        for (String s : list) System.out.print(s + " ");
        System.out.println();

        System.out.print("Reverse: ");
        Iterator<String> rev = list.reverseIterator();
        while (rev.hasNext()) System.out.print(rev.next() + " ");
        System.out.println();
    }
}
```

---

## 17. Mediator

**Purpose:** Define an object that encapsulates how a set of objects interact.

**Use case:** Chat rooms, aircraft traffic control, GUI dialogs (form validation).

```java
/*
 *  +----------+       +-----------+
 *  | Colleague|------>| Mediator  |
 *  +----------+       +-----------+
 *       /\              | +notify |
 *       |               +-----------+
 *  +--------+                /\
 *  |Concrete|               /  \
 *  |Colleague|         +------+  +------+
 *  +--------+         |Concrete|  |Concrete|
 *                      |Mediator|  |Colleague|
 *                      +--------+  +------+
 */

import java.util.*;

// Mediator interface
interface ChatMediator {
    void sendMessage(String msg, User user);
    void addUser(User user);
}

// Concrete mediator
class ChatRoom implements ChatMediator {
    private List<User> users = new ArrayList<>();

    @Override public void addUser(User user) {
        users.add(user);
    }

    @Override public void sendMessage(String msg, User sender) {
        for (User u : users) {
            if (u != sender) {
                u.receive(msg, sender.getName());
            }
        }
    }
}

// Colleague
abstract class User {
    protected ChatMediator mediator;
    protected String name;

    public User(String name, ChatMediator mediator) {
        this.name = name;
        this.mediator = mediator;
    }

    public String getName() { return name; }
    public abstract void send(String msg);
    public abstract void receive(String msg, String from);
}

// Concrete colleague
class ChatUser extends User {
    public ChatUser(String name, ChatMediator mediator) { super(name, mediator); }

    @Override public void send(String msg) {
        System.out.println(this.name + " sends: " + msg);
        mediator.sendMessage(msg, this);
    }

    @Override public void receive(String msg, String from) {
        System.out.println(this.name + " received from " + from + ": " + msg);
    }
}

public class MediatorDemo {
    public static void main(String[] args) {
        ChatMediator chat = new ChatRoom();

        User alice = new ChatUser("Alice", chat);
        User bob = new ChatUser("Bob", chat);
        User charlie = new ChatUser("Charlie", chat);

        chat.addUser(alice);
        chat.addUser(bob);
        chat.addUser(charlie);

        alice.send("Hello everyone!");
        bob.send("Hey Alice!");
    }
}
```

---

## 18. Memento

**Purpose:** Capture and externalize an object's internal state without violating encapsulation.

**Use case:** Undo/redo, checkpoints, save-game systems.

```java
/*
 *  +--------+      +---------+      +----------+
 *  |Originator|--->|Memento  |<-----| Caretaker |
 *  +--------+      +---------+      +----------+
 *  | -state  |      | -state  |      | -history |
 *  | +save() |      | +get..  |      | +undo    |
 *  | +restore|      +---------+      +----------+
 *  +--------+
 */

import java.util.Stack;

// Memento (immutable)
final class Memento {
    private final String state;

    public Memento(String state) { this.state = state; }

    public String getState() { return state; }
}

// Originator
class TextEditor {
    private StringBuilder content = new StringBuilder();

    public void write(String text) {
        content.append(text);
    }

    public String getContent() {
        return content.toString();
    }

    public Memento save() {
        return new Memento(content.toString());
    }

    public void restore(Memento m) {
        content = new StringBuilder(m.getState());
    }
}

// Caretaker
class History {
    private Stack<Memento> snapshots = new Stack<>();

    public void push(Memento m) { snapshots.push(m); }
    public Memento pop()        { return snapshots.isEmpty() ? null : snapshots.pop(); }
}

public class MementoDemo {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        History history = new History();

        editor.write("Hello ");
        history.push(editor.save());

        editor.write("World ");
        history.push(editor.save());

        editor.write("!!!");
        System.out.println("Current: " + editor.getContent());

        editor.restore(history.pop());
        System.out.println("Undo 1:  " + editor.getContent());

        editor.restore(history.pop());
        System.out.println("Undo 2:  " + editor.getContent());
    }
}
```

---

## 19. Observer

**Purpose:** Define a one-to-many dependency so that when one object changes state, all dependents are notified.

**Use case:** Event listeners, GUI listeners, stock market tickers.

```java
/*
 *  +----------+          +------------+
 *  |Subject   |<>------->| Observer   |
 *  +----------+   N      +------------+
 *  | +attach  |  1       | +update    |
 *  | +detach  |          +------------+
 *  | +notify  |               /\
 *  +----------+               ||
 *                         +--------+
 *                         |Concrete|
 *                         |Observer|
 *                         +--------+
 */

import java.util.*;

// Observer interface
interface Observer {
    void update(String stock, double price);
}

// Subject
class StockMarket {
    private Map<String, Double> stocks = new HashMap<>();
    private List<Observer> observers = new ArrayList<>();

    public void attach(Observer o) { observers.add(o); }
    public void detach(Observer o) { observers.remove(o); }

    public void setPrice(String symbol, double price) {
        stocks.put(symbol, price);
        notifyObservers(symbol, price);
    }

    private void notifyObservers(String symbol, double price) {
        for (Observer o : observers) {
            o.update(symbol, price);
        }
    }
}

// Concrete observer
class Investor implements Observer {
    private String name;

    public Investor(String name) { this.name = name; }

    @Override public void update(String stock, double price) {
        System.out.println(name + " notified: " + stock + " now $" + price);
    }
}

// Another observer
class TradingBot implements Observer {
    @Override public void update(String stock, double price) {
        if (price > 150.0) {
            System.out.println("[BOT] Selling " + stock + " at $" + price);
        } else {
            System.out.println("[BOT] Holding " + stock + " at $" + price);
        }
    }
}

public class ObserverDemo {
    public static void main(String[] args) {
        StockMarket market = new StockMarket();

        Investor alice = new Investor("Alice");
        Investor bob = new Investor("Bob");
        TradingBot bot = new TradingBot();

        market.attach(alice);
        market.attach(bob);
        market.attach(bot);

        market.setPrice("AAPL", 145.00);
        market.setPrice("GOOGL", 2800.00);
        market.setPrice("AAPL", 152.00); // triggers bot sell
    }
}
```

---

## 20. State

**Purpose:** Allow an object to alter its behavior when its internal state changes.

**Use case:** Vending machines, order processing, TCP connections, game character states.

```java
/*
 *  +----------+     +-----------+
 *  | Context  |---->| State     |<interface>
 *  +----------+     +-----------+
 *  | +request |     | +handle   |
 *  +----------+     +-----------+
 *                         /\
 *                    +--------+  +--------+
 *                    |StateA  |  |StateB  |
 *                    +--------+  +--------+
 */

// State interface
interface VendingState {
    void insertCoin(VendingMachine machine);
    void selectProduct(VendingMachine machine);
    void dispense(VendingMachine machine);
}

// Context
class VendingMachine {
    private int stock = 1;
    private VendingState state;

    public VendingMachine() {
        state = new NoCoinState();
    }

    public void setState(VendingState state) { this.state = state; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public void insertCoin()    { state.insertCoin(this); }
    public void selectProduct() { state.selectProduct(this); }
    public void dispense()      { state.dispense(this); }
}

// Concrete states
class NoCoinState implements VendingState {
    @Override public void insertCoin(VendingMachine m) {
        System.out.println("Coin inserted.");
        m.setState(new HasCoinState());
    }

    @Override public void selectProduct(VendingMachine m) {
        System.out.println("Insert coin first.");
    }

    @Override public void dispense(VendingMachine m) {
        System.out.println("Insert coin first.");
    }
}

class HasCoinState implements VendingState {
    @Override public void insertCoin(VendingMachine m) {
        System.out.println("Already has a coin.");
    }

    @Override public void selectProduct(VendingMachine m) {
        if (m.getStock() > 0) {
            System.out.println("Product selected.");
            m.setState(new DispensingState());
        } else {
            System.out.println("Out of stock.");
            m.setState(new NoCoinState());
        }
    }

    @Override public void dispense(VendingMachine m) {
        System.out.println("Select product first.");
    }
}

class DispensingState implements VendingState {
    @Override public void insertCoin(VendingMachine m) {
        System.out.println("Wait, dispensing in progress.");
    }

    @Override public void selectProduct(VendingMachine m) {
        System.out.println("Already dispensing.");
    }

    @Override public void dispense(VendingMachine m) {
        System.out.println("Dispensing item...");
        m.setStock(m.getStock() - 1);
        m.setState(new NoCoinState());
    }
}

public class StateDemo {
    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine();

        vm.selectProduct();   // insert coin first
        vm.insertCoin();      // coin inserted
        vm.insertCoin();      // already has coin
        vm.selectProduct();   // product selected
        vm.dispense();        // dispensing...
        vm.selectProduct();   // insert coin first (back to NoCoin)
    }
}
```

---

## 21. Strategy

**Purpose:** Define a family of algorithms, encapsulate each one, and make them interchangeable.

**Use case:** Payment methods, compression algorithms, sorting strategies, validation rules.

```java
/*
 *  +----------+     +------------+
 *  | Context  |---->| Strategy   |<interface>
 *  +----------+     +------------+
 *  | +execute |     | +execute   |
 *  +----------+     +------------+
 *                         /\
 *                    +--------+  +--------+
 *                    |Concrete|  |Concrete|
 *                    |StratA  |  |StratB  |
 *                    +--------+  +--------+
 */

// Strategy interface
interface PaymentStrategy {
    void pay(double amount);
}

// Concrete strategies
class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;

    public CreditCardPayment(String cardNumber) { this.cardNumber = cardNumber; }

    @Override public void pay(double amount) {
        System.out.printf("Paid $%.2f with Credit Card %s%n", amount, cardNumber);
    }
}

class PayPalPayment implements PaymentStrategy {
    private String email;

    public PayPalPayment(String email) { this.email = email; }

    @Override public void pay(double amount) {
        System.out.printf("Paid $%.2f via PayPal (%s)%n", amount, email);
    }
}

class CryptoPayment implements PaymentStrategy {
    private String wallet;

    public CryptoPayment(String wallet) { this.wallet = wallet; }

    @Override public void pay(double amount) {
        System.out.printf("Paid $%.2f with Crypto (%s)%n", amount, wallet);
    }
}

// Context
class ShoppingCart {
    private PaymentStrategy paymentMethod;

    public void setPaymentMethod(PaymentStrategy pm) { this.paymentMethod = pm; }

    public void checkout(double total) {
        if (paymentMethod == null) {
            System.out.println("No payment method selected!");
        } else {
            paymentMethod.pay(total);
        }
    }
}

public class StrategyDemo {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();

        cart.setPaymentMethod(new CreditCardPayment("1234-5678-9012-3456"));
        cart.checkout(99.99);

        cart.setPaymentMethod(new PayPalPayment("user@example.com"));
        cart.checkout(49.99);

        cart.setPaymentMethod(new CryptoPayment("0xABC123"));
        cart.checkout(199.00);
    }
}
```

---

## 22. Template Method

**Purpose:** Define the skeleton of an algorithm in a method, deferring some steps to subclasses.

**Use case:** Build processes, data mining frameworks, game AI skeletons.

```java
/*
 *  +--------------+
 *  | AbstractClass|
 *  +--------------+
 *  | +template()  |   <-- algorithm skeleton
 *  | #step1()     |
 *  | #step2()     |
 *  | #hook()      |   <-- optional override
 *  +--------------+
 *        /\
 *  +-----------+  +-----------+
 *  |ConcreteA  |  |ConcreteB  |
 *  +-----------+  +-----------+
 */

abstract class DataMiner {
    // Template method (final to prevent overriding)
    public final void mine(String file) {
        openFile(file);
        extractData();
        parseData();
        analyze();
        closeFile();
        if (shouldSendReport()) {
            sendReport();
        }
    }

    protected abstract void openFile(String file);
    protected abstract void extractData();
    protected abstract void parseData();

    // common step
    private void analyze() {
        System.out.println("Analyzing data...");
    }

    private void closeFile() {
        System.out.println("Closing file.");
    }

    // hook - optional override
    protected boolean shouldSendReport() { return false; }

    private void sendReport() {
        System.out.println("Sending report...");
    }
}

class PDFMiner extends DataMiner {
    @Override protected void openFile(String file) {
        System.out.println("Opening PDF: " + file);
    }

    @Override protected void extractData() {
        System.out.println("Extracting text from PDF.");
    }

    @Override protected void parseData() {
        System.out.println("Parsing PDF data.");
    }

    @Override protected boolean shouldSendReport() { return true; }
}

class CSVDataMiner extends DataMiner {
    @Override protected void openFile(String file) {
        System.out.println("Opening CSV: " + file);
    }

    @Override protected void extractData() {
        System.out.println("Reading CSV rows.");
    }

    @Override protected void parseData() {
        System.out.println("Parsing CSV fields.");
    }
}

public class TemplateMethodDemo {
    public static void main(String[] args) {
        System.out.println("=== PDF ===");
        DataMiner pdf = new PDFMiner();
        pdf.mine("report.pdf");

        System.out.println("\n=== CSV ===");
        DataMiner csv = new CSVDataMiner();
        csv.mine("data.csv");
    }
}
```

---

# Enterprise/Modern Patterns

---

## 23. DAO/Repository

**Purpose:** Abstract and encapsulate all access to a data source.

**Use case:** Database operations, CRUD abstraction, testing with in-memory stores.

```java
/*
 *  +----------+       +-----------+
 *  | Client   |------>| DAO/Repo  |<interface>
 *  +----------+       +-----------+
 *                      | +findById |
 *                      | +save     |
 *                      | +delete   |
 *                      +-----------+
 *                           /\
 *                      +-----------+  +-----------+
 *                      |InMemory   |  |Database   |
 *                      |Repository |  |Repository |
 *                      +-----------+  +-----------+
 */

import java.util.*;

// Entity
class User {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id; this.name = name; this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }

    @Override public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}

// Repository / DAO interface
interface UserRepository {
    User findById(Long id);
    List<User> findAll();
    void save(User user);
    void delete(Long id);
}

// In-memory implementation
class InMemoryUserRepository implements UserRepository {
    private Map<Long, User> store = new HashMap<>();
    private long nextId = 1;

    @Override public User findById(Long id) { return store.get(id); }

    @Override public List<User> findAll() { return new ArrayList<>(store.values()); }

    @Override public void save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        store.put(user.getId(), user);
    }

    @Override public void delete(Long id) { store.remove(id); }
}

public class DAODemo {
    public static void main(String[] args) {
        UserRepository repo = new InMemoryUserRepository();

        repo.save(new User(null, "Alice", "alice@example.com"));
        repo.save(new User(null, "Bob", "bob@example.com"));

        System.out.println("All users:");
        repo.findAll().forEach(System.out::println);

        User u = repo.findById(1L);
        System.out.println("Found: " + u);

        repo.delete(1L);
        System.out.println("After deletion: " + repo.findAll().size() + " users");
    }
}
```

---

## 24. Dependency Injection

**Purpose:** Invert the creation and binding of dependencies from the class to an external container.

**Use case:** Loose coupling, testability (mock injection), framework design (Spring, Guice).

```java
/*
 *  +----------+     +-----------+      +-----------+
 *  | Client   |---->| Service   |<-----| Injector  |
 *  +----------+     |(interface)|      +-----------+
 *                   +-----------+      | +provide  |
 *                           /\         +-----------+
 *                      +--------+
 *                      |Impl X  |
 *                      +--------+
 */

import java.util.*;

// Service interface
interface MessageService {
    void send(String msg, String recipient);
}

// Implementation A
class EmailService implements MessageService {
    @Override public void send(String msg, String recipient) {
        System.out.println("Email to " + recipient + ": " + msg);
    }
}

// Implementation B
class SMSService implements MessageService {
    @Override public void send(String msg, String recipient) {
        System.out.println("SMS to " + recipient + ": " + msg);
    }
}

// Client (depends on abstraction, not concrete)
class NotificationService {
    private final MessageService messageService;

    // Dependency injected via constructor
    public NotificationService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void notify(String msg, String user) {
        messageService.send(msg, user);
    }
}

// Simple DI Container
class DIContainer {
    private Map<Class<?>, Class<?>> bindings = new HashMap<>();

    public <T> void register(Class<T> iface, Class<? extends T> impl) {
        bindings.put(iface, impl);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> iface) {
        Class<?> impl = bindings.get(iface);
        if (impl == null) throw new RuntimeException("No binding for " + iface);
        try {
            return (T) impl.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + impl, e);
        }
    }
}

public class DIDemo {
    public static void main(String[] args) {
        // Manual DI
        NotificationService emailNotifier = new NotificationService(new EmailService());
        emailNotifier.notify("Hello!", "alice@example.com");

        NotificationService smsNotifier = new NotificationService(new SMSService());
        smsNotifier.notify("Hello!", "+1234567890");

        // DI Container
        DIContainer container = new DIContainer();
        container.register(MessageService.class, EmailService.class);
        MessageService svc = container.resolve(MessageService.class);
        NotificationService viaContainer = new NotificationService(svc);
        viaContainer.notify("From container", "bob@example.com");
    }
}
```

---

## 25. Null Object

**Purpose:** Provide a default no-op object that avoids null checks and NullPointerException.

**Use case:** Logging (NullLogger), optional dependencies, default behaviors.

```java
/*
 *  +----------+
 *  | Interface|
 *  +----------+
 *       /\          /\
 *      /            \
 *  +--------+   +---------+
 *  |Concrete|   |NullImpl |
 *  |Impl    |   |(no-op)  |
 *  +--------+   +---------+
 */

// Interface
interface Logger {
    void info(String msg);
    void warn(String msg);
    void error(String msg);
}

// Real implementation
class ConsoleLogger implements Logger {
    @Override public void info(String msg)  { System.out.println("[INFO] " + msg); }
    @Override public void warn(String msg)  { System.out.println("[WARN] " + msg); }
    @Override public void error(String msg) { System.out.println("[ERROR] " + msg); }
}

// Null Object (no-op)
class NullLogger implements Logger {
    @Override public void info(String msg)  {}
    @Override public void warn(String msg)  {}
    @Override public void error(String msg) {}
}

// Service that uses logger
class OrderService {
    private final Logger logger;

    public OrderService(Logger logger) {
        this.logger = logger != null ? logger : new NullLogger();
    }

    public void placeOrder(String item) {
        logger.info("Placing order for: " + item);
        System.out.println("Order placed: " + item);
        logger.info("Order completed.");
    }
}

public class NullObjectDemo {
    public static void main(String[] args) {
        OrderService withLogging = new OrderService(new ConsoleLogger());
        withLogging.placeOrder("Laptop");

        System.out.println();

        // Without logger - no null checks needed
        OrderService withoutLogging = new OrderService(null);
        withoutLogging.placeOrder("Mouse");
    }
}
```

---

## 26. Specification

**Purpose:** Encapsulate business rules into reusable, composable logical units.

**Use case:** Validation pipelines, query filtering, business rule engines.

```java
/*
 *  +-------------+       +-----------+
 *  | Filter<T>   |       |Spec<T>   |<interface>
 *  +-------------+       +-----------+
 *  | +filter(List)|       | +isSatisfied|
 *  +-------------+       | +and/or/not|
 *                         +-----------+
 */

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Product class
class Product {
    private String name;
    private double price;
    private String category;
    private boolean inStock;

    public Product(String name, double price, String category, boolean inStock) {
        this.name = name; this.price = price; this.category = category; this.inStock = inStock;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isInStock() { return inStock; }

    @Override public String toString() {
        return name + " ($" + price + ", " + category + ")";
    }
}

// Specification interface (with composition methods)
interface Spec<T> {
    boolean isSatisfiedBy(T item);

    default Spec<T> and(Spec<T> other) {
        return item -> this.isSatisfiedBy(item) && other.isSatisfiedBy(item);
    }

    default Spec<T> or(Spec<T> other) {
        return item -> this.isSatisfiedBy(item) || other.isSatisfiedBy(item);
    }

    default Spec<T> not() {
        return item -> !this.isSatisfiedBy(item);
    }
}

// Concrete specifications
class PriceSpec implements Spec<Product> {
    private double maxPrice;
    public PriceSpec(double maxPrice) { this.maxPrice = maxPrice; }
    @Override public boolean isSatisfiedBy(Product p) { return p.getPrice() <= maxPrice; }
}

class CategorySpec implements Spec<Product> {
    private String category;
    public CategorySpec(String category) { this.category = category; }
    @Override public boolean isSatisfiedBy(Product p) { return p.getCategory().equals(category); }
}

class InStockSpec implements Spec<Product> {
    @Override public boolean isSatisfiedBy(Product p) { return p.isInStock(); }
}

// Filter service
class ProductFilter {
    public List<Product> filter(List<Product> products, Spec<Product> spec) {
        return products.stream()
            .filter(spec::isSatisfiedBy)
            .collect(Collectors.toList());
    }
}

public class SpecificationDemo {
    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
            new Product("Laptop", 1200, "Electronics", true),
            new Product("Mouse", 25, "Electronics", true),
            new Product("Desk", 350, "Furniture", false),
            new Product("Chair", 200, "Furniture", true),
            new Product("Phone", 800, "Electronics", true)
        );

        ProductFilter filter = new ProductFilter();

        // Composite specification: Electronics AND in stock AND under $1000
        Spec<Product> cheapElectronics = new CategorySpec("Electronics")
            .and(new InStockSpec())
            .and(new PriceSpec(1000));

        List<Product> results = filter.filter(products, cheapElectronics);
        System.out.println("Cheap electronics in stock:");
        results.forEach(System.out::println);
    }
}
```

---

## 27. Pipeline/Fluent Builder

**Purpose:** Chain operations sequentially where each step processes and passes data to the next.

**Use case:** ETL pipelines, image processing, data transformation workflows.

```java
/*
 *  +----------+     +----------+     +----------+
 *  | Stage 1  |---->| Stage 2  |---->| Stage 3  |
 *  +----------+     +----------+     +----------+
 *  | +process |     | +process |     | +process |
 *  +----------+     +----------+     +----------+
 */

import java.util.*;
import java.util.function.Function;

// Pipeline stage
class PipelineStage<T, R> {
    private final Function<T, R> function;
    private final String name;

    public PipelineStage(String name, Function<T, R> function) {
        this.name = name;
        this.function = function;
    }

    @SuppressWarnings("unchecked")
    public <V> PipelineStage<T, V> next(PipelineStage<R, V> nextStage) {
        Function<T, V> combined = function.andThen(nextStage.function);
        return new PipelineStage<>(name + " -> " + nextStage.name, combined);
    }

    public R execute(T input) {
        System.out.println("Executing " + name);
        return function.apply(input);
    }

    public String getName() { return name; }
}

// Fluent Builder for the pipeline
class PipelineBuilder<T> {
    private PipelineStage<T, T> current;

    public PipelineBuilder(T initialValue) {
        // identity stage
        this.current = new PipelineStage<>("start", x -> x);
    }

    public PipelineBuilder<T> addStage(String name, Function<T, T> stage) {
        PipelineStage<T, T> newStage = new PipelineStage<>(name, stage);
        current = current.next(newStage);
        return this;
    }

    public T execute() {
        return current.execute(null); // start value handled by identity
    }
}

// More practical: String pipeline
class StringPipeline {
    public static String process(String input, List<Function<String, String>> stages) {
        String result = input;
        for (Function<String, String> stage : stages) {
            result = stage.apply(result);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== Pipeline with stages ===");

        List<Function<String, String>> pipeline = Arrays.asList(
            s -> s.trim(),
            s -> s.toLowerCase(),
            s -> s.replaceAll("\\s+", "_"),
            s -> s.substring(0, Math.min(s.length(), 20))
        );

        String result = StringPipeline.process("  Hello World from Pipeline!  ", pipeline);
        System.out.println("Result: '" + result + "'");

        System.out.println("\n=== Fluent Builder Pipeline ===");

        // Using PipelineStage composition
        PipelineStage<String, String> stage1 = new PipelineStage<>("trim", String::trim);
        PipelineStage<String, String> stage2 = new PipelineStage<>("lowercase", String::toLowerCase);
        PipelineStage<String, String> stage3 = new PipelineStage<>("replaceSpaces",
            s -> s.replace(' ', '-'));

        String output = stage1.next(stage2).next(stage3).execute("  HELLO WORLD  ");
        System.out.println("Fluent pipeline: '" + output + "'");
    }
}
```

---

## Compilation & Running

All examples above are standard Java. Compile and run any example:

```bash
# Save the pattern as <PatternName>.java
javac <PatternName>.java
java <PatternName>
```

For inner classes in a single file, the enclosing class is the one with `main()`.

---

## Summary Table

| Category | Pattern | Intent |
|---|---|---|
| Creational | Singleton | One instance, global access |
| Creational | Factory Method | Subclasses decide which class to instantiate |
| Creational | Abstract Factory | Families of related products |
| Creational | Builder | Step-by-step construction |
| Creational | Prototype | Clone instances |
| Structural | Adapter | Match incompatible interfaces |
| Structural | Bridge | Decouple abstraction from implementation |
| Structural | Composite | Tree structures with uniform operations |
| Structural | Decorator | Add responsibilities dynamically |
| Structural | Facade | Simplified interface to a subsystem |
| Structural | Flyweight | Share fine-grained objects |
| Structural | Proxy | Surrogate to control access |
| Behavioral | Chain of Responsibility | Pass request along a chain |
| Behavioral | Command | Request as object (undo/redo) |
| Behavioral | Interpreter | Evaluate language expressions |
| Behavioral | Iterator | Sequential access to a collection |
| Behavioral | Mediator | Encapsulate interaction between objects |
| Behavioral | Memento | Capture/restore object state |
| Behavioral | Observer | One-to-many notification |
| Behavioral | State | Behavior changes with internal state |
| Behavioral | Strategy | Interchangeable algorithms |
| Behavioral | Template Method | Algorithm skeleton, subclasses fill steps |
| Enterprise | DAO/Repository | Data access abstraction |
| Enterprise | Dependency Injection | Invert dependency creation |
| Enterprise | Null Object | No-op default to avoid null checks |
| Enterprise | Specification | Composable business rules |
| Enterprise | Pipeline/Fluent Builder | Sequential processing stages |
