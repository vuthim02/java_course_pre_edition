plugins {
    java
}

group = "com.elite"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
