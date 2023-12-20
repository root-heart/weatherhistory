import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // there is currently no spock-groovy-version-combination that works with JDK 20. perhaps drop it completely?
//    groovy
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

    implementation("org.jetbrains.exposed:exposed:0.17.14")

    implementation(project(":common")) {
        isTransitive = true
    }

//    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
//    no newer version for genesis. dates back to 2016...
//    testImplementation("com.nagternal:spock-genesis:0.6.0")
//    testImplementation("org.mock-server:mockserver-netty:5.15.0")
//    no newer version for http-builder. dates back to 2014...
//    testImplementation("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks.test {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.7"
}