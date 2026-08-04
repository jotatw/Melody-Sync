package com.melodysync.platform.installation

import com.melodysync.platform.shell.CommandResult
import com.melodysync.platform.shell.ShellExecutor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class InstallationValidatorTest {

    @TempDir
    lateinit var tmp: Path

    private class FakeShell(private val succeeds: Boolean) : ShellExecutor() {
        override fun run(
            command: List<String>,
            workingDir: Path?,
            onLine: ((String) -> Unit)?,
        ): CommandResult = CommandResult(
            command = command.joinToString(" "),
            stdout = if (succeeds) "/usr/bin/java\n" else "",
            stderr = "",
            exitCode = if (succeeds) 0 else 1,
            durationMillis = 0,
        )
    }

    private fun createCheckout(): Path {
        val dir = tmp.resolve("repo")
        Files.createDirectories(dir.resolve("scripts"))
        Files.writeString(dir.resolve("gradlew"), "#!/bin/sh\n")
        Files.writeString(dir.resolve("build.gradle.kts"), "plugins {}\n")
        Files.writeString(dir.resolve("gradle.properties"), "melodySyncVersion=0.12.0-dev\n")
        Files.writeString(dir.resolve("scripts/install.sh"), "#!/usr/bin/env bash\n")
        return dir
    }

    @Test
    fun `valid checkout passes project validation`() {
        val validator = InstallationValidator()

        assertTrue(validator.validateProject(createCheckout()).isEmpty())
        assertTrue(validator.isSourceCheckout(createCheckout()))
    }

    @Test
    fun `missing project files are reported`() {
        val validator = InstallationValidator()
        val dir = tmp.resolve("empty")
        Files.createDirectories(dir)

        val issues = validator.validateProject(dir)

        assertFalse(issues.isEmpty())
        assertFalse(validator.isSourceCheckout(dir))
        assertTrue(issues.any { it.check == "gradlew" })
        assertTrue(issues.any { it.check == "scripts/install.sh" })
    }

    @Test
    fun `null or missing project dir is reported`() {
        val validator = InstallationValidator()

        assertFalse(validator.isSourceCheckout(null))
        assertTrue(validator.validateProject(null).any { it.check == "project" })
        assertTrue(validator.validateProject(tmp.resolve("nope")).any { it.check == "project" })
    }

    @Test
    fun `environment validation passes when commands exist`() {
        val validator = InstallationValidator(FakeShell(succeeds = true))

        assertTrue(validator.validateEnvironment().isEmpty())
    }

    @Test
    fun `environment validation reports missing java and bash`() {
        val validator = InstallationValidator(FakeShell(succeeds = false))

        val issues = validator.validateEnvironment()

        assertTrue(issues.any { it.check == "java" })
        assertTrue(issues.any { it.check == "bash" })
        assertEquals(2, issues.size)
    }
}
