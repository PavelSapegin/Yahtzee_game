import org.gradle.kotlin.dsl.application

plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    application
}

application {
    mainClass.set("application.MainKt")
}
group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.exposed:exposed-core:0.50.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
