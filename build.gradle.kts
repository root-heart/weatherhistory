plugins {
    kotlin("jvm") version "1.5.10"
    groovy
    java
}

group = "rootheart.codes"
version = "0.0.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/exposed")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
    implementation("org.jetbrains.exposed:exposed:0.17.14")
    implementation("org.codehaus.groovy:groovy-all:3.0.8")
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("com.h2database:h2:2.0.202")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("net.lingala.zip4j:zip4j:2.9.0")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("com.nagternal:spock-genesis:0.6.0")
    testImplementation("org.mock-server:mockserver-netty:5.11.2")
    testImplementation("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
