package com.melodysync.platform.installation

import com.melodysync.platform.shell.CommandResult
import com.melodysync.platform.shell.ShellExecutor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class InstallationServiceTest {

    @TempDir
    lateinit var tmp: Path

    private class FakeShell(
        var envSucceeds: Boolean = true,
        var installSucceeds: Boolean = true,
    ) : ShellExecutor() {
        var installCalls = 0

        override fun run(
            command: List<String>,
            workingDir: Path?,
            onLine: ((String) -> Unit)?,
        ): CommandResult {
            val joined = command.joinToString(" ")
            if (joined.contains("install.sh")) {
                installCalls++
                return CommandResult(
                    command = joined,
                    stdout = "==> Building\n==> Installing to\n",
                    stderr = if (installSucceeds) "" else "gradle build failed",
                    exitCode = if (installSucceeds) 0 else 1,
                    durationMillis = 1,
                )
            }
            return CommandResult(
                command = joined,
                stdout = if (envSucceeds) "/usr/bin/java\n/usr/bin/bash\n" else "",
                stderr = "",
                exitCode = if (envSucceeds) 0 else 1,
                durationMillis = 1,
            )
        }
    }

    private fun createCheckout(version: String = "0.12.0-dev"): Path {
        val dir = tmp.resolve("repo")
        Files.createDirectories(dir.resolve("scripts"))
        Files.writeString(dir.resolve("gradlew"), "#!/bin/sh\n")
        Files.writeString(dir.resolve("build.gradle.kts"), "plugins {}\n")
        Files.writeString(dir.resolve("gradle.properties"), "melodySyncVersion=$version\n")
        Files.writeString(dir.resolve("scripts/install.sh"), "#!/usr/bin/env bash\n")
        return dir
    }

    private fun createInstallDir(version: String?): Path {
        val dir = tmp.resolve("install")
        if (version != null) {
            Files.createDirectories(dir)
            Files.writeString(dir.resolve("VERSION"), "$version\n")
        }
        return dir
    }

    @Test
    fun `detectInstallation returns null when nothing installed`() {
        val service = InstallationService(InstallationValidator(FakeShell(true)), FakeShell(true))

        assertNull(service.detectInstallation(tmp.resolve("empty")))
    }

    @Test
    fun `detectInstallation reads the version file when no json exists`() {
        val service = InstallationService(InstallationValidator(FakeShell(true)), FakeShell(true))
        val dir = createInstallDir("0.12.0-dev")

        val info = service.detectInstallation(dir)

        assertEquals("0.12.0-dev", info?.version)
    }

    @Test
    fun `update refuses when not a source checkout`() {
        val service = InstallationService(InstallationValidator(FakeShell(true)), FakeShell(true))
        val result = service.update(tmp.resolve("not-a-repo"), tmp.resolve("install"))

        assertFalse(result.installed)
        assertFalse(result.sourceBased)
        assertTrue(result.message.contains("not installed from source"))
    }

    @Test
    fun `update reports already up to date without rebuilding`() {
        val shell = FakeShell(true)
        val service = InstallationService(InstallationValidator(shell), shell)
        val result = service.update(createCheckout(), createInstallDir("0.12.0-dev"))

        assertFalse(result.installed)
        assertTrue(result.sourceBased)
        assertTrue(result.message.contains("Already up to date"))
        assertEquals(0, shell.installCalls)
    }

    @Test
    fun `update force rebuilds even when versions match`() {
        val shell = FakeShell(true)
        val service = InstallationService(InstallationValidator(shell), shell)
        val installDir = createInstallDir("0.12.0-dev")

        val result = service.update(createCheckout(), installDir, build = "Desktop", force = true)

        assertTrue(result.installed)
        assertTrue(result.sourceBased)
        assertTrue(result.message.contains("Installed"))
        assertEquals(1, shell.installCalls)
        val json = InstallationInfo.load(installDir.resolve("INSTALLATION.json"))
        assertTrue(json != null)
        assertTrue(json!!.sourceBased)
        assertEquals("Desktop", json.build)
    }

    @Test
    fun `update reports install failure`() {
        val shell = FakeShell(installSucceeds = false)
        val service = InstallationService(InstallationValidator(shell), shell)

        val result = service.update(createCheckout(), tmp.resolve("install"), force = true)

        assertFalse(result.installed)
        assertTrue(result.message.contains("Install failed"))
    }

    @Test
    fun `update reports missing environment tools`() {
        val shell = FakeShell(envSucceeds = false)
        val service = InstallationService(InstallationValidator(shell), shell)

        val result = service.update(createCheckout(), tmp.resolve("install"))

        assertFalse(result.installed)
        assertTrue(result.message.contains("java"))
    }

    @Test
    fun `checkForUpdate reports update available and up to date`() {
        val shell = FakeShell()
        val service = InstallationService(InstallationValidator(shell), shell)

        val available = service.checkForUpdate(
            createCheckout("0.13.0"),
            createInstallDir("0.12.0-dev"),
        )
        assertTrue(available.sourceBased)
        assertTrue(available.updateAvailable)
        assertEquals("0.13.0", available.sourceVersion)
        assertEquals("0.12.0-dev", available.installedVersion)

        val same = service.checkForUpdate(
            createCheckout("0.12.0-dev"),
            createInstallDir("0.12.0-dev"),
        )
        assertTrue(same.sourceBased)
        assertFalse(same.updateAvailable)
    }

    @Test
    fun `checkForUpdate refuses when not a source checkout`() {
        val shell = FakeShell()
        val service = InstallationService(InstallationValidator(shell), shell)

        val check = service.checkForUpdate(tmp.resolve("not-a-repo"), tmp.resolve("install"))

        assertFalse(check.sourceBased)
        assertFalse(check.updateAvailable)
        assertTrue(check.message.orEmpty().contains("not installed from source"))
    }

    @Test
    fun `update reports missing melodySyncVersion`() {
        val shell = FakeShell(true)
        val service = InstallationService(InstallationValidator(shell), shell)
        val dir = createCheckout()
        Files.writeString(dir.resolve("gradle.properties"), "kotlin.code.style=official\n")

        val result = service.update(dir, tmp.resolve("install"))

        assertFalse(result.installed)
        assertTrue(result.message.contains("melodySyncVersion"))
    }
}
