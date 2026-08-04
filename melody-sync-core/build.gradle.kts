plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)

    implementation(libs.jaudiotagger)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikaricp)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
}

val generateVersionResource = tasks.register("generateVersionResource") {
    val version = providers.gradleProperty("melodySyncVersion").get()
    val outputDir = layout.buildDirectory.dir("generated/resources/version")
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("melody-sync-version.properties").writeText("version=$version\n")
    }
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated/resources/version"))
}

tasks.named("processResources") {
    dependsOn(generateVersionResource)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}