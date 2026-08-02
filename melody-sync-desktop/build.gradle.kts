import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    google()
}

dependencies {
    implementation(project(":melody-sync-core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.foundation)
    implementation(compose.materialIconsExtended)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

compose.desktop {
    application {
        mainClass = "com.melodysync.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "melody-sync"
            packageVersion = "0.10.0"
            description = "Organize, analyze and explore your local music library."
            vendor = "Melody Sync"
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}