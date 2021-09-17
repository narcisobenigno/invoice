plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    constraints {
        implementation("org.json:json:20210307")

        compileOnly("org.projectlombok:lombok:1.18.20")
        annotationProcessor("org.projectlombok:lombok:1.18.20")

        testCompileOnly("org.projectlombok:lombok:1.18.20")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
}

tasks {
    val integrationTest = register<Test>("integration") {
        description = "Runs integration tests."
        group = "verification"

        shouldRunAfter("test")
        useJUnitPlatform {
            includeTags("integration")
        }
    }

    check {
        dependsOn(integrationTest)
    }

    test {
        useJUnitPlatform {
            excludeTags("integration")
            includeEngines("junit-jupiter")
        }
    }
}
