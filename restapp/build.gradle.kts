plugins {
    kotlin("jvm")
}

group = "rootheart.codes.weatherhistory"
version = "0.0.1"

repositories {
    mavenCentral()
}

val ktorVersion = "1.6.7"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("joda-time:joda-time:2.10.14")

    implementation(project(":common"))

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("com.nagternal:spock-genesis:0.6.0")
    testImplementation("org.mock-server:mockserver-netty:5.11.2")
    testImplementation("org.codehaus.groovy.modules.http-builder:http-builder:0.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
}