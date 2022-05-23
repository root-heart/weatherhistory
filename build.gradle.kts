plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
    groovy
    java
    application
}

group = "rootheart.codes"
version = "0.0.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/exposed")
    }
}


subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
        implementation("ch.qos.logback:logback-classic:1.2.10")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
    implementation("org.codehaus.groovy:groovy-all:3.0.8")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("com.nagternal:spock-genesis:0.6.0")
    testImplementation("org.mock-server:mockserver-netty:5.11.2")
    testImplementation("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("rootheart.codes.weatherhistory.rest.WeatherHistoryApplicationKt")
}