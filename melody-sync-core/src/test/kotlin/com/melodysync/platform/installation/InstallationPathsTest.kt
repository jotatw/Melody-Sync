package com.melodysync.platform.installation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class InstallationPathsTest {

    @TempDir
    lateinit var tmp: Path

    @Test
    fun `default install dir lives under user local share`() {
        val dir = InstallationPaths.defaultInstallDir()

        assertTrue(dir.toString().contains(".local"))
        assertTrue(dir.toString().endsWith("melody-sync"))
    }

    @Test
    fun `installDir override is absolute and normalized`() {
        assertEquals(
            tmp.toAbsolutePath().normalize(),
            InstallationPaths.installDir(tmp),
        )
    }

    @Test
    fun `artifacts resolve under the install dir`() {
        val dir = tmp.resolve("data")

        assertEquals(dir.resolve("melody-sync.jar"), InstallationPaths.jarFile(dir))
        assertEquals(dir.resolve("VERSION"), InstallationPaths.versionFile(dir))
        assertEquals(dir.resolve("INSTALLATION.json"), InstallationPaths.installationJson(dir))
    }

    @Test
    fun `readInstalledVersion reads and trims the version file`() {
        val dir = tmp.resolve("data")
        Files.createDirectories(dir)
        Files.writeString(dir.resolve("VERSION"), "0.12.0-dev\n")

        assertEquals("0.12.0-dev", InstallationPaths.readInstalledVersion(dir))
    }

    @Test
    fun `readInstalledVersion returns null when missing or blank`() {
        val dir = tmp.resolve("data")

        assertNull(InstallationPaths.readInstalledVersion(dir))

        Files.createDirectories(dir)
        Files.writeString(dir.resolve("VERSION"), "   ")
        assertNull(InstallationPaths.readInstalledVersion(dir))
    }
}
