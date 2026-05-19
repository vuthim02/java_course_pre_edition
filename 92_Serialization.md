# Lesson 92: Serialization

## Key Concepts
- Serialization converts an object into a byte stream for storage or network transmission
- Deserialization reconstructs an object from a byte stream
- A class must implement `Serializable` (a marker interface) to be serializable
- `ObjectOutputStream` writes objects; `ObjectInputStream` reads them back
- `transient` fields are skipped during serialization
- `serialVersionUID` identifies the class version for deserialization compatibility

## Code Example

```java
import java.io.*;

public class Main {
    public static void main(String[] args) {
        User user1 = new User("Alice", "alice@example.com", 25);
        User user2 = new User("Bob", "bob@example.com", 30);

        String filename = "users.ser";

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(user1);
            out.writeObject(user2);
            System.out.println("Users serialized to " + filename);
        } catch (IOException e) {
            System.out.println("Error during serialization: " + e.getMessage());
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            User deserialized1 = (User) in.readObject();
            User deserialized2 = (User) in.readObject();

            System.out.println("\nDeserialized users:");
            System.out.println(deserialized1);
            System.out.println(deserialized2);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during deserialization: " + e.getMessage());
        }
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;

    String name;
    transient String email;
    int age;

    User(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', email='" + email + "', age=" + age + "}";
    }
}
```

## Explanation
1. `User implements Serializable` — the marker interface that enables serialization.
2. `serialVersionUID = 1L` — a version ID that ensures the sender and receiver of a serialized object have loaded compatible classes.
3. `transient String email` — The `email` field is marked `transient`, so it will NOT be serialized. When deserialized, it will be `null`.
4. `ObjectOutputStream` wraps a `FileOutputStream` to write objects to a file.
5. `ObjectInputStream` wraps a `FileInputStream` to read objects back (must cast).
6. The try-with-resources statement automatically closes the streams.

## Expected Output

```
Users serialized to users.ser

Deserialized users:
User{name='Alice', email='null', age=25}
User{name='Bob', email='null', age=30}
```

Notice `email` is `null` because it was marked `transient`.
