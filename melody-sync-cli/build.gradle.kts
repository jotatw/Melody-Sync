plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":melody-sync-core"))
    implementation(libs.clikt)
    implementation(libs.mordant)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)

    testImplementation(project(":melody-sync-core"))
    testImplementation(libs.clikt)
    testImplementation(libs.mordant)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass.set("com.melodysync.cli.MainKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}