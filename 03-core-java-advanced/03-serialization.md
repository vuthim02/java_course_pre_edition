# Core Java Advanced — Lesson 3: Serialization

## What is Serialization?

**Serialization** = converting an object into a byte stream (for saving to file, sending over network)
**Deserialization** = reconstructing the object from the byte stream

```
Object (in memory)                    Object (in memory)
┌──────────────────┐                 ┌──────────────────┐
│ Person           │                 │ Person           │
│ name: "Alice"    │  Serialize      │ name: "Alice"    │
│ age: 30          │ ──────────────▶ │ age: 30          │
│ ssn: "123-45"    │                 │ ssn: "123-45"    │
└──────────────────┘                 └──────────────────┘
        │                                    ▲
        │                                    │
        ▼                                    │
┌──────────────────┐     file/network        │
│ 3F 52 00 24 ...  │ ────────────────────────┘
│ (byte stream)    │    Deserialize
└──────────────────┘
```

## Making a Class Serializable

A class must implement the `Serializable` **marker interface** (no methods — just marks the class):

```java
import java.io.Serializable;

public class Person implements Serializable {
    private static final long serialVersionUID = 1L;  // IMPORTANT!

    private String name;
    private int age;
    private transient String ssn;  // Won't be serialized!

    public Person(String name, int age, String ssn) {
        this.name = name;
        this.age = age;
        this.ssn = ssn;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + ", ssn='" + ssn + "'}";
    }
}
```

## Serializing and Deserializing

```java
// SERIALIZE: Object → bytes
Person person = new Person("Alice", 30, "123-45-6789");

try (FileOutputStream fos = new FileOutputStream("person.ser");
     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
    oos.writeObject(person);
    System.out.println("Serialized: " + person);
} catch (IOException e) {
    e.printStackTrace();
}

// DESERIALIZE: bytes → Object
try (FileInputStream fis = new FileInputStream("person.ser");
     ObjectInputStream ois = new ObjectInputStream(fis)) {
    Person loaded = (Person) ois.readObject();
    System.out.println("Deserialized: " + loaded);
    // ssn will be null (marked transient)
} catch (IOException | ClassNotFoundException e) {
    e.printStackTrace();
}
```

## The `serialVersionUID`

**CRITICAL:** Always declare `serialVersionUID`:

```java
private static final long serialVersionUID = 1L;
```

If you change the class (add/remove fields) and don't declare this ID, Java generates one automatically — but it WILL change between compilations, causing `InvalidClassException` during deserialization.

```java
// Version 1 — serialized
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int age;
}

// Version 2 — deserializing v1's data
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;  // SAME ID
    private String name;
    private int age;
    private String email;  // New field → will get default value (null)
}
```

## The `transient` Keyword

Fields marked `transient` are **skipped** during serialization:

```java
public class UserSession implements Serializable {
    private String username;
    private transient String password;      // Won't be saved!
    private transient Connection connection; // Can't serialize network connections!
}
```

**Use `transient` for:** Passwords, connections, threads, any field that can be reconstructed.

## Custom Serialization

Override `writeObject` and `readObject` for custom logic:

```java
public class SensitiveData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String data;
    private transient String encryptedData;

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();  // Write normal fields
        // Custom: encrypt data before writing
        oos.writeObject(encrypt(data));
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();  // Read normal fields
        // Custom: decrypt after reading
        this.encryptedData = (String) ois.readObject();
        this.data = decrypt(encryptedData);
    }

    private String encrypt(String s) { /* encryption logic */ }
    private String decrypt(String s) { /* decryption logic */ }
}
```

## Modern Alternative: JSON/XML/YAML

For most modern applications, prefer text-based formats:

```java
// Jackson JSON (most common)
ObjectMapper mapper = new ObjectMapper();

// Serialize to JSON
String json = mapper.writeValueAsString(person);

// Deserialize from JSON
Person person = mapper.readValue(json, Person.class);

// Write to file
mapper.writeValue(new File("person.json"), person);

// Read from file
Person person = mapper.readValue(new File("person.json"), Person.class);
```

---

### Exercises

1. Create a `Book` class with title, author, year, and a transient `isbn` field. Serialize and deserialize it.
2. Add `serialVersionUID` to a class, serialize it, then add a new field. Deserialize — what happens to the new field?
3. Use Jackson to serialize and deserialize an object to JSON.
4. Demonstrate the security risk: serialize an object with a password, then inspect the raw bytes.
