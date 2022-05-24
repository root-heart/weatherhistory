plugins {
    kotlin("jvm")
    groovy
}

group = "rootheart.codes.weatherhistory"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("joda-time:joda-time:2.10.14")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    implementation(project(":common")) {
        isTransitive = true
    }

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("com.nagternal:spock-genesis:0.6.0")
    testImplementation("org.mock-server:mockserver-netty:5.11.2")
    testImplementation("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}