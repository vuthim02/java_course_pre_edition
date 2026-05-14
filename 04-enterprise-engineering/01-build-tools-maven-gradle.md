# Enterprise Engineering — Lesson 1: Build Tools (Maven & Gradle)

## Why Build Tools?

Before build tools, Java developers:
- Compiled by hand: `javac *.java`
- Managed JARs manually: download, add to classpath
- Built JARs by hand: `jar cf myapp.jar *.class`
- No standard project structure

Build tools automate ALL of this:
- Dependency management
- Compilation
- Testing
- Packaging
- Deployment

## Maven

### Project Object Model (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### Maven Directory Structure

```
my-app/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/          ← Source code
│   │   │   └── com/example/
│   │   │       └── App.java
│   │   └── resources/     ← Properties, XML, configs
│   │       └── application.properties
│   └── test/
│       ├── java/          ← Tests
│       │   └── com/example/
│       │       └── AppTest.java
│       └── resources/     ← Test resources
└── target/                ← Build output (generated)
```

### Essential Maven Commands

```bash
mvn clean          # Delete target/ directory
mvn compile        # Compile source code
mvn test           # Run tests
mvn package        # Compile + test + package (JAR/WAR)
mvn install        # Package + install to local repository
mvn deploy         # Install + deploy to remote repository
mvn validate       # Validate project is correct
mvn verify         # Run integration tests

# Common combinations:
mvn clean install          # Clean then install
mvn clean package -DskipTests  # Package without tests
mvn dependency:tree        # Print dependency tree
mvn help:effective-pom     # Print effective POM
```

### Dependency Scopes

| Scope | Purpose | Example |
|-------|---------|---------|
| `compile` (default) | Available everywhere | Core libraries |
| `provided` | Provided by JDK/server at runtime | Servlet API, Lombok |
| `runtime` | Needed at runtime, not compile | JDBC drivers |
| `test` | Only for tests | JUnit, Mockito |
| `system` | System JAR (explicit path) | Rare, avoid |

### Maven Lifecycle Phases

```
validate ─▶ compile ─▶ test ─▶ package ─▶ verify ─▶ install ─▶ deploy
    │          │         │         │          │          │         │
    ▼          ▼         ▼         ▼          ▼          ▼         ▼
  Validate   Compile   Run      Create     Run        Copy to  Deploy to
  pom.xml    source    tests    JAR/WAR   integration  local    remote
                                   │       tests       repo     repo
                                   ▼
                               target/*.jar
```

## Gradle

### build.gradle

```groovy
plugins {
    id 'java'
    id 'application'
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    implementation 'com.google.guava:guava:32.1.3-jre'
}

application {
    mainClass = 'com.example.App'
}
```

### Kotlin DSL (build.gradle.kts)

```kotlin
plugins {
    java
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    implementation("com.google.guava:guava:32.1.3-jre")
}

application {
    mainClass.set("com.example.App")
}
```

### Gradle Commands

```bash
gradle clean           # Delete build/
gradle build           # Compile + test + package
gradle test            # Run tests
gradle run             # Run the application
gradle assemble        # Package without tests
gradle check           # Run verification tasks
gradle tasks           # List available tasks
```

## Maven vs Gradle

| Aspect | Maven | Gradle |
|--------|-------|--------|
| **Configuration** | XML (verbose) | Groovy/Kotlin DSL (concise) |
| **Performance** | Slower (no incremental) | Faster (incremental, daemon) |
| **Convention** | Strict, opinionated | Flexible |
| **Learning curve** | Easier | Steeper |
| **Dependency management** | Mature, deterministic | Mature, deterministic |
| **Build caching** | Limited | Excellent |
| **Market share** | Higher (Spring ecosystem) | Growing fast |

**Which to use:** Maven for enterprise (standard). Gradle for Android and modern projects.

---

### Exercises

1. Create a Maven project with JUnit dependency. Run `mvn test`.
2. Add a third-party library (e.g., Apache Commons Lang) to your Maven project. Use `StringUtils.reverse()` in your code.
3. Create a multi-module Maven project with a `core` and `web` module.
4. Convert the Maven project to Gradle.
5. Run `mvn dependency:tree` and understand the dependency hierarchy.
