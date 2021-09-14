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
    }


    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}

tasks.test {
    useJUnitPlatform()
}
