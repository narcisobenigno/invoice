plugins {
    java
}

repositories {
    mavenCentral()
}


sourceSets {
    create("integrationTest") {
        java.srcDirs("src/integration/java")
        resources.srcDirs("src/integration/resources")
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().compileClasspath
        runtimeClasspath += output + sourceSets.main.get().output + sourceSets.test.get().runtimeClasspath
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val integrationTestAnnotationProcessor: Configuration by configurations.getting {
    extendsFrom(configurations.annotationProcessor.get())
}

val integrationTestCompileOnly: Configuration by configurations.getting {
    extendsFrom(configurations.compileOnly.get())
}

val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

dependencies {
    constraints {
        implementation("org.json:json:20210307")

        compileOnly("org.projectlombok:lombok:1.18.20")
        annotationProcessor("org.projectlombok:lombok:1.18.20")

        testCompileOnly("org.projectlombok:lombok:1.18.20")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.20")

        integrationTestCompileOnly("org.projectlombok:lombok:1.18.20")
        integrationTestAnnotationProcessor("org.projectlombok:lombok:1.18.20")

    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")

    integrationTestImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    integrationTestImplementation("org.skyscreamer:jsonassert:1.5.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
