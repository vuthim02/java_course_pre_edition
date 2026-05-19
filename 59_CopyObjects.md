# Lesson 59: Copying Objects

## Key Concepts
- Objects are copied **by reference** by default; assignment (`car2 = car1`) makes both variables point to the same object
- To create an independent copy, you must copy the field values yourself
- A **copy method** (`copy(Car other)`) copies field values from one object to another
- A **copy constructor** (`Car(Car other)`) creates a new object initialized from an existing one
- Without explicit copying, modifying one reference affects the other

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Car car1 = new Car("Toyota", "Camry", 2022);
        Car car2 = new Car("Honda", "Civic", 2023);

        System.out.println("Before copy:");
        System.out.println("Car 1: " + car1);
        System.out.println("Car 2: " + car2);

        car2.copy(car1);
        System.out.println("\nAfter copying car1 into car2:");
        System.out.println("Car 1: " + car1);
        System.out.println("Car 2: " + car2);

        Car car3 = new Car(car1);
        System.out.println("\nCar 3 (copy constructor): " + car3);
    }
}

class Car {
    private String make;
    private String model;
    private int year;

    Car(String make, String model, int year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }

    Car(Car other) {
        this.copy(other);
    }

    void copy(Car other) {
        this.make = other.make;
        this.model = other.model;
        this.year = other.year;
    }

    @Override
    public String toString() {
        return year + " " + make + " " + model;
    }
}
```

### Explanation
`car2.copy(car1)` copies each field from `car1` into `car2`. After this, both objects have the same data but are **independent** — changing one does not affect the other. The copy constructor `Car(Car other)` calls the `copy()` method internally, providing a clean way to create a duplicate at instantiation time.

### Expected Output
```
Before copy:
Car 1: 2022 Toyota Camry
Car 2: 2023 Honda Civic

After copying car1 into car2:
Car 1: 2022 Toyota Camry
Car 2: 2022 Toyota Camry

Car 3 (copy constructor): 2022 Toyota Camry
```
