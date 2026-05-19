# Lesson 49: Array of Objects

## Key Concepts
- Arrays can hold objects just like primitives
- Syntax: `ClassName[] arrayName = new ClassName[size];`
- After creating the array, each element must be individually instantiated
- You can also use an **array initializer** with curly braces: `{new Obj(...), new Obj(...)}`
- Enhanced for-loops (`for (Type var : array)`) make iterating over object arrays clean

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("How many students? ");
        int numStudents = scanner.nextInt();
        scanner.nextLine();

        Student[] students = new Student[numStudents];

        for (int i = 0; i < students.length; i++) {
            System.out.println("\nEnter details for student " + (i + 1) + ":");
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Age: ");
            int age = scanner.nextInt();
            scanner.nextLine();

            students[i] = new Student(name, age);
        }

        System.out.println("\n=== STUDENT LIST ===");
        for (Student student : students) {
            System.out.println(student);
        }

        Food[] fridge = {
            new Food("Pizza"),
            new Food("Burger"),
            new Food("Salad")
        };

        System.out.println("\n=== FOOD IN FRIDGE ===");
        for (Food food : fridge) {
            System.out.println(food);
        }

        scanner.close();
    }
}

class Student {
    String name;
    int age;

    Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return name + " (" + age + " years old)";
    }
}

class Food {
    String name;

    Food(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
```

### Explanation
The program first builds an array of `Student` objects from user input. It then demonstrates an array literal with `Food` objects. Both use the enhanced for-loop to print each element (relying on the overridden `toString()`).

### Expected Output (example)
```
How many students? 2

Enter details for student 1:
Name: Alice
Age: 20

Enter details for student 2:
Name: Bob
Age: 22

=== STUDENT LIST ===
Alice (20 years old)
Bob (22 years old)

=== FOOD IN FRIDGE ===
Pizza
Burger
Salad
```
