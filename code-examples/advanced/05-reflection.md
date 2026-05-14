# Reflection API and Dynamic Proxies

This document covers the Java Reflection API including class inspection, field/method/constructor access, annotation retrieval, array reflection, dynamic proxies, and the module API.

## Target Class for Reflection Examples

```java
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@interface Important {
    String value() default "";
}

@Retention(RetentionPolicy.RUNTIME)
@interface InvokeMe {
    String description() default "";
}

class ExampleService {
    private String name = "DefaultService";
    public int version = 1;
    private static final double PI = 3.14159;

    public ExampleService() {}

    public ExampleService(String name) {
        this.name = name;
    }

    private ExampleService(String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Important("core functionality")
    public void serve() {
        System.out.println("Service.serve() called");
    }

    @InvokeMe(description = "prints greeting")
    private void secretMethod(String greeting) {
        System.out.println("Secret says: " + greeting);
    }

    @Important
    private static int calculate(int x, int y) {
        return x + y;
    }

    @Override
    public String toString() {
        return "ExampleService{name='" + name + "', version=" + version + "}";
    }
}
```

## Class Inspection

```java
public class ReflectionClassDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Class.forName() and .class literal ---");

        // Three ways to get a Class
        Class<?> c1 = Class.forName("ExampleService");      // by name (string)
        Class<?> c2 = ExampleService.class;                   // .class literal
        Class<?> c3 = new ExampleService().getClass();         // via instance

        System.out.println("Same class: " + (c1 == c2 && c2 == c3));
        System.out.println("Class name: " + c1.getName());
        System.out.println("Simple name: " + c1.getSimpleName());
        System.out.println("Package: " + c1.getPackageName());
        System.out.println("Superclass: " + c1.getSuperclass().getSimpleName());

        // Interfaces
        System.out.println("\nInterfaces:");
        for (Class<?> iface : c1.getInterfaces()) {
            System.out.println("  " + iface.getName());
        }

        // Modifiers
        System.out.println("Is public: " + java.lang.reflect.Modifier.isPublic(c1.getModifiers()));
    }
}
```

## Fields, Methods, and Constructors

```java
import java.lang.reflect.*;

public class ReflectionMembersDemo {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = ExampleService.class;

        System.out.println("--- Declared Fields ---");
        // getFields() — only public (including inherited)
        // getDeclaredFields() — all fields in this class (including private)
        for (Field field : clazz.getDeclaredFields()) {
            System.out.println("  " + java.lang.reflect.Modifier.toString(field.getModifiers())
                + " " + field.getType().getSimpleName() + " " + field.getName());
        }

        System.out.println("\n--- Declared Methods ---");
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("  " + method.getName() + "("
                + java.util.Arrays.toString(method.getParameterTypes())
                    .replace("[", "").replace("]", "") + ")");
        }

        System.out.println("\n--- Declared Constructors ---");
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            System.out.println("  " + ctor.getName() + "("
                + java.util.Arrays.toString(ctor.getParameterTypes())
                    .replace("[", "").replace("]", "") + ")");
        }
    }
}
```

## Accessing Private Fields (get/set with setAccessible)

```java
import java.lang.reflect.*;

public class FieldAccessDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Field get/set (private via setAccessible) ---");

        ExampleService service = new ExampleService();
        Class<?> clazz = service.getClass();

        // Public field access
        Field versionField = clazz.getField("version");
        System.out.println("Public field 'version': " + versionField.get(service));
        versionField.set(service, 42);
        System.out.println("After set: " + versionField.get(service));

        // Private field access — need setAccessible(true)
        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true); // bypass access control (may fail with module system)
        System.out.println("Private field 'name': " + nameField.get(service));
        nameField.set(service, "ModifiedService");
        System.out.println("After set: " + nameField.get(service));

        System.out.println("Service toString: " + service);

        // Private static final field
        Field piField = clazz.getDeclaredField("PI");
        piField.setAccessible(true);
        System.out.println("Private static final PI: " + piField.get(null));
    }
}
```

## Invoking Methods via Reflection

```java
import java.lang.reflect.*;

public class MethodInvokeDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Method.invoke() ---");

        ExampleService service = new ExampleService();
        Class<?> clazz = service.getClass();

        // Invoke public method
        Method serveMethod = clazz.getMethod("serve");
        serveMethod.invoke(service);

        // Invoke private method
        Method secretMethod = clazz.getDeclaredMethod("secretMethod", String.class);
        secretMethod.setAccessible(true);
        Object result = secretMethod.invoke(service, "Hello from reflection!");
        System.out.println("Private method returned: " + result); // void -> null

        // Invoke private static method
        Method calculateMethod = clazz.getDeclaredMethod("calculate", int.class, int.class);
        calculateMethod.setAccessible(true);
        int sum = (int) calculateMethod.invoke(null, 10, 20);
        System.out.println("Private static calculate(10,20) = " + sum);
    }
}
```

## Constructor.newInstance

```java
import java.lang.reflect.*;

public class ConstructorDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Constructor newInstance ---");

        Class<?> clazz = ExampleService.class;

        // Default constructor
        Constructor<?> defaultCtor = clazz.getConstructor();
        ExampleService s1 = (ExampleService) defaultCtor.newInstance();
        System.out.println("Default ctor: " + s1);

        // Public parameterized constructor
        Constructor<?> publicCtor = clazz.getConstructor(String.class);
        ExampleService s2 = (ExampleService) publicCtor.newInstance("CustomName");
        System.out.println("Public ctor: " + s2);

        // Private parameterized constructor
        Constructor<?> privateCtor = clazz.getDeclaredConstructor(String.class, int.class);
        privateCtor.setAccessible(true);
        ExampleService s3 = (ExampleService) privateCtor.newInstance("PrivateInit", 99);
        System.out.println("Private ctor: " + s3);
    }
}
```

## Annotations at Runtime

```java
import java.lang.reflect.*;

public class AnnotationDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Runtime Annotations ---");

        Class<?> clazz = ExampleService.class;

        // Class-level annotations
        System.out.println("Class annotations:");
        for (var ann : clazz.getAnnotations()) {
            System.out.println("  @" + ann.annotationType().getSimpleName());
        }

        // Method-level annotations
        System.out.println("\nMethods with @Important:");
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Important.class)) {
                Important imp = method.getAnnotation(Important.class);
                System.out.println("  " + method.getName() + " -> value='" + imp.value() + "'");
            }
            if (method.isAnnotationPresent(InvokeMe.class)) {
                InvokeMe inv = method.getAnnotation(InvokeMe.class);
                System.out.println("  " + method.getName() + " -> description='" + inv.description() + "'");
            }
        }
    }
}
```

## Arrays via Reflection

```java
import java.lang.reflect.*;

public class ArrayReflectionDemo {
    public static void main(String[] args) {
        System.out.println("--- Array.newInstance ---");

        // Create an int array of size 5
        int[] intArray = (int[]) Array.newInstance(int.class, 5);
        for (int i = 0; i < intArray.length; i++) {
            Array.set(intArray, i, i * 10);
        }
        System.out.print("intArray: ");
        for (int i = 0; i < intArray.length; i++) {
            System.out.print(Array.get(intArray, i) + " ");
        }
        System.out.println();

        // Create a String array
        String[] strArray = (String[]) Array.newInstance(String.class, 3);
        Array.set(strArray, 0, "Alpha");
        Array.set(strArray, 1, "Beta");
        Array.set(strArray, 2, "Gamma");
        System.out.println("strArray[0] = " + Array.get(strArray, 0));

        // Multi-dimensional array
        int[][] matrix = (int[][]) Array.newInstance(int.class, 3, 4);
        System.out.println("Matrix dimensions: " + Array.getLength(matrix) + "x" + Array.getLength(Array.get(matrix, 0)));
    }
}
```

## Dynamic Proxy

```java
import java.lang.reflect.*;
import java.util.*;

// An interface for proxying
interface DataService {
    String fetchData(String key);
    void saveData(String key, String value);
}

// Real implementation
class RealDataService implements DataService {
    @Override
    public String fetchData(String key) {
        System.out.println("  [RealService] Fetching: " + key);
        return "data_for_" + key;
    }

    @Override
    public void saveData(String key, String value) {
        System.out.println("  [RealService] Saving: " + key + " = " + value);
    }
}

// InvocationHandler — intercepts all method calls
class LoggingHandler implements InvocationHandler {
    private final Object target;

    LoggingHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("[Proxy] Before: " + method.getName() + (args != null ? " " + Arrays.toString(args) : ""));
        try {
            Object result = method.invoke(target, args);
            System.out.println("[Proxy] After: " + method.getName() + " -> " + result);
            return result;
        } catch (InvocationTargetException e) {
            System.err.println("[Proxy] Exception: " + e.getCause().getMessage());
            throw e.getCause();
        }
    }
}

public class DynamicProxyDemo {
    public static void main(String[] args) {
        System.out.println("--- Dynamic Proxy ---");

        RealDataService realService = new RealDataService();

        DataService proxy = (DataService) Proxy.newProxyInstance(
            DataService.class.getClassLoader(),
            new Class<?>[]{DataService.class},
            new LoggingHandler(realService)
        );

        // All calls go through the InvocationHandler
        proxy.saveData("user1", "Alice");
        String result = proxy.fetchData("user1");
        System.out.println("Result: " + result);

        System.out.println("\nIs proxy: " + Proxy.isProxyClass(proxy.getClass()));
    }
}
```

## Module API

```java
import java.lang.module.*;

public class ModuleApiDemo {
    public static void main(String[] args) {
        System.out.println("--- Module API ---");

        // Get module of a class
        Module module = String.class.getModule();
        System.out.println("Module of String: " + module.getName());
        System.out.println("Is named: " + module.isNamed());
        System.out.println("Is exported: " + module.isExported("java.lang"));

        // Layer information
        ModuleLayer layer = ModuleLayer.boot();
        System.out.println("\nBoot layer modules (" + layer.modules().size() + "):");

        // Find a specific module
        layer.findModule("java.base").ifPresent(mod -> {
            ModuleDescriptor descriptor = mod.getDescriptor();
            System.out.println("\nModule: " + descriptor.name());
            System.out.println("  Exports:");
            descriptor.exports().forEach(e ->
                System.out.println("    " + e.source() + " -> " + e.targets()));
            System.out.println("  Packages: " + descriptor.packages());

            // Requires (dependencies)
            System.out.println("  Requires:");
            descriptor.requires().forEach(r ->
                System.out.println("    " + r.name() + " ("
                    + r.modifiers().stream().map(Object::toString).reduce((a,b) -> a + "," + b).orElse("none")
                    + ")"));
        });
    }
}
```
