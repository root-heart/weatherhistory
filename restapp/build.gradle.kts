plugins {
    kotlin("jvm")
    id("io.ktor.plugin") version "2.1.3"
    kotlin("plugin.serialization")
}

group = "rootheart.codes.weatherhistory"
version = "0.0.1"

repositories {
    mavenCentral()
}

val ktor_version = "2.1.3"

dependencies {
    implementation(kotlin("stdlib"))

    // TODO I would like to not directly depend on exposed in the restapp
    implementation("org.jetbrains.exposed:exposed:0.17.14")

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")

    implementation("joda-time:joda-time:2.10.14")

    implementation(project(":common"))

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("com.nagternal:spock-genesis:0.6.0")
    testImplementation("org.mock-server:mockserver-netty:5.11.2")
    testImplementation("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
}

application {
    mainClass.set("rootheart.codes.weatherhistory.restapp.WeatherHistoryApplicationKt")
}