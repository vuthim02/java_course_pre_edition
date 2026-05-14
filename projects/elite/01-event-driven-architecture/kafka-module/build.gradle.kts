dependencies {
    implementation("org.apache.kafka:kafka-clients:3.6.0")
    implementation("org.apache.kafka:kafka-streams:3.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.apache.kafka:kafka_2.13:3.6.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}
