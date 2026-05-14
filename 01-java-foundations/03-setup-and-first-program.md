# Java Foundations — Lesson 3: Setup & First Java Program

## Install Java with SDKMAN (Recommended — all platforms)

SDKMAN is a version manager for Java — think "nvm for Node.js" or "pyenv for Python."

```bash
# Step 1: Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Step 2: List available Java distributions
sdk list java

# Step 3: Install Java 21 LTS (Temurin — Eclipse's build)
sdk install java 21.0.2-tem

# Step 4: Verify
java --version
# Output: openjdk 21.0.2 2024-01-16 LTS
# Output: OpenJDK 64-Bit Server VM (build 21.0.2+13, mixed mode, sharing)

# Step 5: Set as default
sdk default java 21.0.2-tem
```

### Other Java Distributions

| Distribution | Provider | Best For |
|-----------|----------|----------|
| **Temurin** | Eclipse Adoptium | General purpose, LTS |
| **Corretto** | Amazon Web Services | AWS deployment |
| **GraalVM** | Oracle | Native images, polyglot |
| **Liberica** | BellSoft | Full features, FX bundled |
| **Zulu** | Azul | Enterprise, embedded |

## Install IntelliJ IDEA

```bash
# Download from: https://www.jetbrains.com/idea/download/
# Community Edition is FREE and fully featured

# Or on Ubuntu:
sudo snap install intellij-idea-community --classic
# Or via Toolbox App (recommended — auto-updates):
# Download from jetbrains.com/toolbox-app
```

## Your First Java Program

### 1. Create the file

```bash
mkdir -p ~/java-course/hello-world
cd ~/java-course/hello-world
touch HelloWorld.java
```

### 2. Write the code

```java
// HelloWorld.java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, Java World!");
    }
}
```

**Critical rules:**
- The filename MUST match the class name: `HelloWorld.java` ↔ `public class HelloWorld`
- Java is case-sensitive: `helloworld` ≠ `HelloWorld`
- Every Java app needs a `main` method: `public static void main(String[] args)`

### 3. Compile and run

```bash
# Compile: creates HelloWorld.class
javac HelloWorld.java

# Run: executes the class
java HelloWorld

# Output:
Hello, Java World!
```

### 4. What just happened?

```
┌──────────────┐     javac      ┌──────────────┐    java    ┌──────────────────┐
│ HelloWorld   │ ──────────────▶│ HelloWorld   │ ──────────▶│ "Hello, Java    │
│ .java        │                │ .class       │            │  World!"        │
│ (source)     │                │ (bytecode)   │            │ (output)        │
└──────────────┘                └──────────────┘            └──────────────────┘
```

## Anatomy of a Java Program

```java
// 1. PACKAGE DECLARATION (optional, but recommended)
package com.example.helloworld;

// 2. IMPORTS (optional)
import java.util.Scanner;

// 3. CLASS DECLARATION
public class HelloWorld {
    // 4. MAIN METHOD — entry point
    public static void main(String[] args) {
        // 5. STATEMENTS — what the program does
        System.out.println("Hello, World!");
    }
}
```

### Breaking Down `public static void main(String[] args)`

```
┌─────────┐ ┌────────┐ ┌──────┐ ┌─────────────┐
│ public  │ │ static │ │ void │ │ main(String[])│
└─────────┘ └────────┘ └──────┘ └─────────────┘
     │           │        │            │
     ▼           ▼        ▼            ▼
 Can be      Belongs    Returns    Takes an array
 accessed    to the     nothing    of Strings
 from        CLASS     (no return  (command-line
 anywhere    itself     value)     arguments)
            (not an
            instance)
```

## Install Git

```bash
# Ubuntu/Debian
sudo apt install git -y

# macOS
brew install git

# Windows
winget install Git.Git

# Verify
git --version

# Configure
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

## Install Maven (Build Tool)

```bash
sdk install maven
mvn --version
```

## IDE Setup: IntelliJ IDEA

1. Open IntelliJ IDEA → New Project
2. Name: `hello-world`
3. Language: Java
4. Build System: IntelliJ
5. JDK: 21 (select the one installed via SDKMAN)
6. Create

Explore the interface:
- **Project tool window** (left) — file browser
- **Editor** (center) — write code
- **Run** (top right) — green play button
- **Terminal** (bottom) — built-in terminal

![Key shortcuts: Ctrl+Shift+F10 = Run, Ctrl+/ = Comment, Ctrl+Alt+L = Format]

## Common Compilation Errors & How to Read Them

```java
// ERROR 1: Class name doesn't match filename
public class hello {  // ERROR: class is lowercase, file is HelloWorld.java
// Output: class hello is public, should be declared in a file named hello.java
```

```java
// ERROR 2: Missing main method
public class HelloWorld {
    public static void main() {  // Missing String[] args!
        System.out.println("Hi");
    }
}
// Output: Main method not found in class HelloWorld
```

```java
// ERROR 3: Semicolon missing
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello")  // Missing semicolon!
    }
}
// Output: ';' expected
```

---

### Exercises

1. Write a program that prints your name.
2. Write a program that prints your name, age, and city on separate lines.
3. Compile and run the program from the terminal (not IDE).
4. Try all three common errors above and read the error messages.
5. Create a GitHub account and push your HelloWorld program: `git init && git add . && git commit -m "first java"` (don't push yet — just practice local commits).
