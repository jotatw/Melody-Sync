package com.melodysync.platform.installation

import com.melodysync.platform.shell.ShellExecutor
import java.nio.file.Files
import java.nio.file.Path

/**
 * A single problem found while validating the environment or a source
 * checkout. [check] is a short machine-readable name (e.g. "gradlew").
 */
data class ValidationIssue(
    val check: String,
    val message: String,
)

/**
 * Validates the host environment and whether a directory is a Melody Sync
 * source checkout. Environment and project checks are kept separate so
 * errors are easy to attribute.
 */
class InstallationValidator(
    private val shell: ShellExecutor = ShellExecutor(),
) {

    fun validateEnvironment(): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        if (!isCommandAvailable("java")) {
            issues += ValidationIssue("java", "Java 21 runtime not found on PATH")
        }
        if (!isCommandAvailable("bash")) {
            issues += ValidationIssue("bash", "bash not found on PATH")
        }
        return issues
    }

    fun validateProject(projectDir: Path?): List<ValidationIssue> {
        if (projectDir == null || !Files.isDirectory(projectDir)) {
            return listOf(ValidationIssue("project", "Project directory not found: $projectDir"))
        }

        val issues = mutableListOf<ValidationIssue>()
        if (!Files.isRegularFile(projectDir.resolve("gradlew"))) {
            issues += ValidationIssue("gradlew", "gradlew not found in $projectDir")
        }
        if (!Files.isRegularFile(projectDir.resolve("build.gradle.kts"))) {
            issues += ValidationIssue("build.gradle.kts", "build.gradle.kts not found in $projectDir")
        }
        if (!Files.isRegularFile(projectDir.resolve("gradle.properties"))) {
            issues += ValidationIssue("gradle.properties", "gradle.properties not found in $projectDir")
        }
        if (!Files.isRegularFile(projectDir.resolve("scripts/install.sh"))) {
            issues += ValidationIssue("scripts/install.sh", "scripts/install.sh not found in $projectDir")
        }
        return issues
    }

    fun isSourceCheckout(projectDir: Path?): Boolean =
        validateProject(projectDir).isEmpty()

    private fun isCommandAvailable(name: String): Boolean =
        try {
            val result = shell.run(listOf("bash", "-lc", "command -v $name"))
            result.succeeded && result.stdout.isNotBlank()
        } catch (_: Exception) {
            false
        }
}
