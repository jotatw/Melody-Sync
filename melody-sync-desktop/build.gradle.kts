import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val melodySyncVersion: String by project
val rpmSafeVersion: String = melodySyncVersion.substringBefore('-')

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
    implementation(libs.koalaplot.core)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines.swing)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

compose.desktop {
    application {
        mainClass = "com.melodysync.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "melody-sync"
            packageVersion = melodySyncVersion
            description = "Organize, analyze and explore your local music library."
            vendor = "Melody Sync"
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
                // RPM rejects '-' in version strings; keep the full version
                // for the uber jar naming but use a sanitized value for RPM.
                rpmPackageVersion = rpmSafeVersion
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
