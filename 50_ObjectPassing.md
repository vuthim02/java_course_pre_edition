# Lesson 50: Passing Objects as Arguments

## Key Concepts
- Objects can be passed as arguments to methods
- When you pass an object, you pass a **reference** to it (not a copy)
- The method can access and modify the object's fields
- Object references can be compared with `==` to check if two variables refer to the same object
- This enables building relationships between classes (e.g., a `Garage` managing `Car` objects)

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Garage garage = new Garage();

        Car car1 = new Car("Tesla");
        Car car2 = new Car("BMW");
        Car car3 = new Car("Toyota");

        garage.park(car1);
        garage.park(car2);
        garage.park(car3);

        garage.listCars();

        garage.unpark(car2);
        garage.listCars();
    }
}

class Garage {
    private Car[] cars = new Car[10];
    private int count = 0;

    void park(Car car) {
        if (count < cars.length) {
            cars[count] = car;
            count++;
            System.out.println(car.name + " is parked in the garage.");
        } else {
            System.out.println("Garage is full!");
        }
    }

    void unpark(Car car) {
        for (int i = 0; i < count; i++) {
            if (cars[i] == car) {
                for (int j = i; j < count - 1; j++) {
                    cars[j] = cars[j + 1];
                }
                cars[count - 1] = null;
                count--;
                System.out.println(car.name + " left the garage.");
                return;
            }
        }
        System.out.println(car.name + " is not in the garage.");
    }

    void listCars() {
        System.out.print("Cars in garage: ");
        if (count == 0) {
            System.out.println("None");
            return;
        }
        for (int i = 0; i < count; i++) {
            System.out.print(cars[i].name);
            if (i < count - 1) System.out.print(", ");
        }
        System.out.println();
    }
}

class Car {
    String name;

    Car(String name) {
        this.name = name;
    }
}
```

### Explanation
`Garage.park(Car car)` receives a `Car` reference and stores it in its internal array. `Garage.unpark(Car car)` uses `==` reference comparison to find and remove the exact same object. This demonstrates object identity and how objects interact through method parameters.

### Expected Output
```
Tesla is parked in the garage.
BMW is parked in the garage.
Toyota is parked in the garage.
Cars in garage: Tesla, BMW, Toyota
BMW left the garage.
Cars in garage: Tesla, Toyota
```
