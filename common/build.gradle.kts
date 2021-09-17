/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("invoice.java-library-conventions")
}

tasks {
    integration {
        environment.putIfAbsent(
            "POSTGRES_CREDENTIAL",
            "{\"host\":\"localhost:5432\",\"dbname\":\"invoice\",\"username\":\"user\",\"password\":\"password\"}"
        )
    }
}

dependencies {
    implementation("org.jdbi:jdbi3-core:3.21.0")
    implementation("org.jdbi:jdbi3-postgres:3.21.0")
    implementation("org.postgresql:postgresql:42.2.23")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.json:json")

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
}
