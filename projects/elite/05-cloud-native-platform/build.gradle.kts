plugins {
    java
    application
}

group = "com.cloudnative"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.cloudnative.operator.CloudNativeOperator"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javaoperatorsdk:operator-framework:4.5.0")
    implementation("io.fabric8:kubernetes-client:6.9.2")
    implementation("io.fabric8:kubernetes-model-common:6.9.2")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
