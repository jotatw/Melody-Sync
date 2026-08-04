package com.melodysync.platform.installation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class InstallationInfoTest {

    @TempDir
    lateinit var tmp: Path

    @Test
    fun `save and load round trip`() {
        val info = InstallationInfo(
            version = "v0.12.0-dev",
            installedAt = "2026-08-04T10:20",
            java = "21",
            os = "Linux",
            build = "Desktop",
            projectDir = "/repo",
            installDir = "/home/user/.local/share/melody-sync",
            sourceBased = true,
        )
        val file = tmp.resolve("INSTALLATION.json")

        InstallationInfo.save(info, file)
        val loaded = InstallationInfo.load(file)

        assertEquals(info, loaded)
    }

    @Test
    fun `load returns null for missing or corrupt file`() {
        assertNull(InstallationInfo.load(tmp.resolve("missing.json")))

        val corrupt = tmp.resolve("corrupt.json")
        Files.writeString(corrupt, "not json")
        assertNull(InstallationInfo.load(corrupt))
    }

    @Test
    fun `installerVersion defaults to one`() {
        val info = InstallationInfo(
            version = "v0.12.0-dev",
            installedAt = "2026-08-04T10:20",
            java = "21",
            os = "Linux",
            build = "Desktop",
        )

        assertEquals(1, info.installerVersion)
    }

    @Test
    fun `save creates parent directories`() {
        val file = tmp.resolve("a/b/c/INSTALLATION.json")
        val info = InstallationInfo(
            version = "v0.12.0-dev",
            installedAt = "2026-08-04T10:20",
            java = "21",
            os = "Linux",
            build = "Desktop",
        )

        InstallationInfo.save(info, file)

        assertTrue(Files.exists(file))
    }
}
