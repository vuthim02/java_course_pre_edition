# Serialization and Deserialization

This document covers Java serialization, including the `Serializable` interface, `transient` keyword, `serialVersionUID`, custom serialization, `Externalizable`, object graphs, and serialization sealing via `writeReplace`/`readResolve`.

## Basic Serialization with Serializable

```java
import java.io.*;
import java.time.LocalDate;
import java.util.Objects;

// A basic serializable class
class Person implements Serializable {
    // Explicit serialVersionUID — critical for versioning
    // Without this, JVM generates one from class details, which WILL differ
    // between compilers/versions, causing InvalidClassException
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;

    // transient — skipped during serialization (e.g., sensitive data, derived state)
    private transient String password;
    // LocalDate is not serializable by default, so it's transient
    private transient LocalDate lastLogin;

    public Person(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
        this.lastLogin = LocalDate.now();
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age
            + ", password='" + password + "' (transient, will be null)"
            + ", lastLogin=" + lastLogin + " (transient)}";
    }
}

public class BasicSerializationDemo {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Path file = Files.createTempFile("person_", ".ser");

        System.out.println("--- Basic Serialization ---");
        Person original = new Person("Alice", 30, "secret123");
        System.out.println("Original: " + original);

        // Serialize
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(original);
        }
        System.out.println("Serialized to: " + file);

        // Deserialize
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            Person deserialized = (Person) ois.readObject();
            System.out.println("Deserialized: " + deserialized);
            // Note: password and lastLogin are null/default — lost due to transient
        }

        Files.deleteIfExists(file);
    }
}
```

```java
import java.io.*;
import java.nio.file.*;
```

The import block above is needed; the full file uses the combined code.

## Custom writeObject/readObject

```java
import java.io.*;

// Demonstrates custom serialization logic
class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double salary;
    private String ssn; // sensitive — encrypt in custom writeObject

    public Employee(String name, double salary, String ssn) {
        this.name = name;
        this.salary = salary;
        this.ssn = ssn;
    }

    // Custom serialization logic
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // write the non-transient, non-static fields
        // Encrypt SSN before writing
        String encrypted = "ENC(" + ssn + ")";
        out.writeObject(encrypted);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Decrypt SSN
        String encrypted = (String) in.readObject();
        this.ssn = encrypted.replace("ENC(", "").replace(")", "");
    }

    @Serial
    private void readObjectNoData() throws ObjectStreamException {
        // Called if no data for this class in the stream (e.g., class evolved)
        this.name = "UNKNOWN";
        this.salary = 0.0;
    }

    @Override
    public String toString() {
        return "Employee{name='" + name + "', salary=" + salary + ", ssn='" + ssn + "'}";
    }
}

public class CustomSerializationDemo {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("employee_", ".ser");

        Employee emp = new Employee("Bob", 85000.0, "123-45-6789");
        System.out.println("Original: " + emp);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(emp);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            Employee deserialized = (Employee) ois.readObject();
            System.out.println("Deserialized: " + deserialized);
        }

        Files.deleteIfExists(file);
    }
}
```

## Externalizable Interface

```java
import java.io.*;
import java.nio.file.*;

// Externalizable gives full control over serialization format
class Book implements Externalizable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String author;
    private int year;

    // Mandatory public no-arg constructor for Externalizable
    public Book() {}

    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // Must explicitly write each field
        out.writeUTF(title);
        out.writeUTF(author);
        out.writeInt(year);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // Must read in the same order as written
        this.title = in.readUTF();
        this.author = in.readUTF();
        this.year = in.readInt();
    }

    @Override
    public String toString() {
        return "Book{title='" + title + "', author='" + author + "', year=" + year + "}";
    }
}

public class ExternalizableDemo {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("book_", ".ser");

        Book book = new Book("1984", "George Orwell", 1949);
        System.out.println("Original: " + book);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(book);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            Book deserialized = (Book) ois.readObject();
            System.out.println("Deserialized: " + deserialized);
        }

        Files.deleteIfExists(file);
    }
}
```

## Object Graphs and writeReplace/readResolve

```java
import java.io.*;
import java.nio.file.*;
import java.util.*;

// Demonstrates object graphs (objects referencing other objects)
class Department implements Serializable {
    private static final long serialVersionUID = 1L;

    String name;
    List<TeamMember> members = new ArrayList<>();

    Department(String name) {
        this.name = name;
    }

    void addMember(TeamMember m) {
        members.add(m);
        m.department = this; // circular reference
    }
}

class TeamMember implements Serializable {
    private static final long serialVersionUID = 1L;

    String name;
    Department department; // back-reference

    TeamMember(String name) {
        this.name = name;
    }
}

// writeReplace / readResolve — sealing/proxy pattern
// Used to control which object is serialized/deserialized (e.g., singletons, enums)
class SingletonManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final SingletonManager INSTANCE = new SingletonManager();

    private SingletonManager() {}

    public static SingletonManager getInstance() {
        return INSTANCE;
    }

    // writeReplace — replaces this object before serialization
    @Serial
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    // readResolve — resolves object after deserialization
    @Serial
    private Object readResolve() {
        return INSTANCE;
    }

    // Serialization proxy — prevents direct serialization of the singleton
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        SerializationProxy(SingletonManager original) {}

        @Serial
        private Object readResolve() {
            return SingletonManager.getInstance();
        }
    }
}

public class ObjectGraphDemo {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("graph_", ".ser");

        System.out.println("--- Object Graph Serialization ---");

        Department dept = new Department("Engineering");
        TeamMember alice = new TeamMember("Alice");
        TeamMember bob = new TeamMember("Bob");
        dept.addMember(alice);
        dept.addMember(bob);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(dept);
        }
        System.out.println("Serialized object graph (including circular refs)");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            Department deserializedDept = (Department) ois.readObject();
            System.out.println("Deserialized: " + deserializedDept.name);
            for (TeamMember m : deserializedDept.members) {
                System.out.println("  Member: " + m.name + " -> " + m.department.name);
            }
        }

        Files.deleteIfExists(file);

        // Singleton serialization test
        Path singletonFile = Files.createTempFile("singleton_", ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(singletonFile.toFile()))) {
            oos.writeObject(SingletonManager.getInstance());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(singletonFile.toFile()))) {
            SingletonManager result = (SingletonManager) ois.readObject();
            // Without readResolve, this would be a different instance
            System.out.println("\nSingleton preserved: " + (result == SingletonManager.getInstance()));
        }
        Files.deleteIfExists(singletonFile);
    }
}
```
