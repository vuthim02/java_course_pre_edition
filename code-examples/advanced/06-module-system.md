# Java Module System (JPMS)

This document covers the Java Platform Module System introduced in Java 9, including `module-info.java`, module dependencies, `ServiceLoader`, reflective access, transitive dependencies, and the module layer API.

## Multi-Module Project Structure

```
project/
├── core/
│   ├── src/main/java/module-info.java
│   └── src/main/java/com/example/core/
│       └── Calculator.java
├── api/
│   ├── src/main/java/module-info.java
│   └── src/main/java/com/example/api/
│       ├── CalculatorService.java
│       └── impl/
│           └── CalculatorServiceImpl.java
└── app/
    ├── src/main/java/module-info.java
    └── src/main/java/com/example/app/
        └── Main.java
```

## module-info.java Examples

```java
// === core/src/main/java/module-info.java ===
module com.example.core {
    // Exports the package so other modules can use it
    exports com.example.core;

    // Provides an implementation of a service interface
    provides com.example.api.CalculatorService
        with com.example.core.BasicCalculator;
}
```

```java
// === api/src/main/java/module-info.java ===
module com.example.api {
    // exports with 'to' — qualified export (only visible to listed modules)
    exports com.example.api to com.example.core, com.example.app;

    // 'requires transitive' — anyone who requires this module
    // also gets access to the specified module (avoids redeclaration)
    requires transitive com.example.common;

    // 'uses' declares this module consumes a service
    uses com.example.api.CalculatorService;

    // 'opens' allows reflective access to specified packages
    // Without 'opens', reflection (setAccessible) fails on private members
    opens com.example.api.impl to com.google.gson;
}
```

```java
// === app/src/main/java/module-info.java ===
module com.example.app {
    requires com.example.api;       // depends on api module
    requires com.example.core;      // depends on core module

    // 'opens' entire package for reflection (allows libraries like Jackson/Gson)
    opens com.example.app to com.google.gson;
}
```

## Module Layer API

```java
import java.lang.module.*;
import java.util.*;

// Simulates a module layer with a service interface
interface GreeterService {
    String greet(String name);
}

class EnglishGreeter implements GreeterService {
    @Override
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

class SpanishGreeter implements GreeterService {
    @Override
    public String greet(String name) {
        return "¡Hola, " + name + "!";
    }
}

public class ModuleLayerDemo {
    public static void main(String[] args) {
        System.out.println("--- Module Layer API ---");

        // Boot layer — the modules that ship with the JDK
        ModuleLayer bootLayer = ModuleLayer.boot();
        System.out.println("Boot layer has " + bootLayer.modules().size() + " modules");

        // Find a specific module in the boot layer
        Optional<Module> javaBase = bootLayer.findModule("java.base");
        javaBase.ifPresent(mod -> {
            System.out.println("Found java.base: " + mod.getDescriptor().toNameAndVersion());
            System.out.println("  Packages: " + mod.getDescriptor().packages().size());
        });

        // Check if a module can read another
        Module stringModule = String.class.getModule();
        Module listModule = List.class.getModule();
        System.out.println("\nString's module can read List's module: "
            + stringModule.canRead(listModule));

        // Module descriptors
        System.out.println("\n--- ModuleDescriptor Example ---");
        String.class.getModule().getDescriptor().exports().stream()
            .filter(e -> !e.isQualified())
            .limit(5)
            .forEach(e -> System.out.println("  exports " + e.source()));
    }
}
```

## ServiceLoader with provides/uses

```java
import java.util.*;

// Service interface (in api module)
interface CalculatorService {
    int add(int a, int b);
    int subtract(int a, int b);
    String getImplementationName();
}

// Service implementation (in core module, registered via 'provides')
class SimpleCalculator implements CalculatorService {
    @Override
    public int add(int a, int b) { return a + b; }

    @Override
    public int subtract(int a, int b) { return a - b; }

    @Override
    public String getImplementationName() {
        return "SimpleCalculator v1.0";
    }
}

// Another implementation
class AdvancedCalculator implements CalculatorService {
    @Override
    public int add(int a, int b) { return a + b; }

    @Override
    public int subtract(int a, int b) { return a - b; }

    @Override
    public String getImplementationName() {
        return "AdvancedCalculator v2.0 (with logging)";
    }
}

public class ServiceLoaderDemo {
    public static void main(String[] args) {
        System.out.println("--- ServiceLoader (simulated without module-info) ---");

        // Normally, ServiceLoader.load() discovers implementations via
        // module-info.java 'provides' declarations or META-INF/services files.
        // Here we manually register providers to demonstrate the pattern.

        // In a real JPMS setup:
        // ServiceLoader<CalculatorService> loader = ServiceLoader.load(CalculatorService.class);

        Map<String, CalculatorService> services = new LinkedHashMap<>();
        services.put("simple", new SimpleCalculator());
        services.put("advanced", new AdvancedCalculator());

        System.out.println("Available services:");
        for (var entry : services.entrySet()) {
            CalculatorService svc = entry.getValue();
            System.out.println("  " + entry.getKey() + ": "
                + svc.add(3, 4) + " (from " + svc.getImplementationName() + ")");
        }

        System.out.println("\n--- Module annotations for services ---");
        // In module-info.java:
        //   provides com.example.api.CalculatorService
        //       with com.example.core.SimpleCalculator;
        //
        //   uses com.example.api.CalculatorService;
        //
        // The 'uses' declaration enables ServiceLoader to find providers.
    }
}
```

## Opens for Reflective Access

```java
import java.lang.reflect.*;

// A class that needs reflective access (e.g., from a serialization library)
class UserData {
    private String username = "default_user";
    private String password = "secret123";
    private int level = 1;

    @Override
    public String toString() {
        return "UserData{username='" + username + "', password='" + password + "', level=" + level + "}";
    }
}

public class ReflectiveAccessDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Opens for Reflective Access ---");

        // In module-info.java:
        //   opens com.example.app to com.google.gson;
        //
        // Without 'opens', setAccessible(true) on private members of
        // classes in exported-but-not-opened packages will throw
        // InaccessibleObjectException.

        UserData user = new UserData();
        System.out.println("Before: " + user);

        // Reflection that requires 'opens' for private access
        Class<?> clazz = user.getClass();

        Field usernameField = clazz.getDeclaredField("username");
        usernameField.setAccessible(true); // needs 'opens' in module-info
        usernameField.set(user, "hacker");

        Field levelField = clazz.getDeclaredField("level");
        levelField.setAccessible(true);
        levelField.setInt(user, 99);

        System.out.println("After reflection: " + user);

        System.out.println("\nWithout 'opens com.example.app to ...',");
        System.out.println("the setAccessible calls above would throw");
        System.out.println("InaccessibleObjectException at runtime.");
    }
}
```

## Transitive Dependencies

```java
import java.util.*;

// Demonstrates the effect of 'requires transitive'

// Module structure (simulated):
//
// module com.example.common {
//     exports com.example.common;
// }
//
// module com.example.api {
//     requires transitive com.example.common;
//     // Any module that 'requires com.example.api' automatically
//     // gets access to com.example.common types without needing
//     // their own 'requires com.example.common'.
// }
//
// module com.example.app {
//     requires com.example.api;
//     // Can use both com.example.api AND com.example.common types
//     // thanks to 'requires transitive' in the api module.
// }

// Without 'transitive', app would need:
//   requires com.example.api;
//   requires com.example.common;  // would be required explicitly

public class TransitiveDependenciesDemo {
    public static void main(String[] args) {
        System.out.println("--- Transitive Dependencies ---");

        System.out.println("""
            'requires transitive' means:
            If module A has 'requires transitive M', then any module
            that requires A automatically also reads M.

            Example:
              module com.example.api {
                  requires transitive com.example.common;
                  exports com.example.api;
              }

              module com.example.app {
                  requires com.example.api;  // also gets com.example.common
                  // Can use types from both com.example.api AND com.example.common
              }

            Without 'transitive', each consumer would need its own
            'requires com.example.common' — violating encapsulation
            and causing compilation errors.
            """);
    }
}
```
