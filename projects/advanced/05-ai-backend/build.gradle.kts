plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.aibackend"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spring.io/milestone")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.0-M2")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.apache.pdfbox:pdfbox:3.0.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
