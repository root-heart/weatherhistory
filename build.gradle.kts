plugins {
    kotlin("jvm") version "1.5.10"
    groovy
    java
}

group = "rootheart.codes"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.codehaus.groovy:groovy-all:3.0.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}