plugins {
    kotlin("jvm")
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
}