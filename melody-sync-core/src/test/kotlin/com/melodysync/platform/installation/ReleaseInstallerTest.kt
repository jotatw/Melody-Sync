package com.melodysync.platform.installation

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ReleaseInstallerTest {

    @TempDir
    lateinit var tmp: Path

    private val server = GithubStubServer()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop()
    }

    private fun releaseInfo(version: String = "0.13.0-dev", sha: String? = server.sha256Hex): ReleaseInfo =
        ReleaseInfo(
            tag = "v$version",
            version = version,
            prerelease = true,
            jarUrl = "${server.baseUrl}/download.jar",
            sha256 = sha,
            sizeBytes = server.jarBytes.size.toLong(),
        )

    @Test
    fun `installs jar, version file and installation json`() {
        val installDir = tmp.resolve("install")
        val result = ReleaseInstaller().install(
            release = releaseInfo(),
            installDir = installDir,
            channel = InstallationChannel.STABLE,
        )

        assertTrue(result.installed)
        assertFalse(result.sourceBased)
        assertEquals("0.13.0-dev", result.version)

        val jar = installDir.resolve("melody-sync.jar")
        assertTrue(Files.exists(jar))
        assertTrue(jar.toFile().readBytes().contentEquals(server.jarBytes))

        assertEquals("0.13.0-dev", Files.readString(installDir.resolve("VERSION")).trim())

        val info = InstallationInfo.load(installDir.resolve("INSTALLATION.json"))
        assertTrue(info != null)
        assertEquals("stable", info!!.channel)
        assertFalse(info.sourceBased)
        assertEquals(2, info.installerVersion)
        assertEquals("0.13.0-dev", info.version)
    }

    @Test
    fun `replaces an existing jar`() {
        val installDir = tmp.resolve("install")
        Files.createDirectories(installDir)
        Files.write(installDir.resolve("melody-sync.jar"), byteArrayOf(1, 2, 3))
        Files.writeString(installDir.resolve("VERSION"), "0.12.0\n")

        val result = ReleaseInstaller().install(
            release = releaseInfo(),
            installDir = installDir,
            channel = InstallationChannel.BETA,
        )

        assertTrue(result.installed)
        assertTrue(installDir.resolve("melody-sync.jar").toFile().readBytes().contentEquals(server.jarBytes))
        assertTrue(!Files.exists(installDir.resolve("melody-sync.jar.bak")))
    }

    @Test
    fun `rejects a corrupted download via sha256 mismatch`() {
        val installDir = tmp.resolve("install")
        Files.createDirectories(installDir)
        Files.write(installDir.resolve("melody-sync.jar"), byteArrayOf(9, 9, 9))

        val result = ReleaseInstaller().install(
            release = releaseInfo(sha = "deadbeef"),
            installDir = installDir,
            channel = InstallationChannel.STABLE,
        )

        assertFalse(result.installed)
        assertTrue(result.message.contains("sha256 mismatch"))
        // the previous jar must be left untouched
        assertTrue(installDir.resolve("melody-sync.jar").toFile().readBytes().contentEquals(byteArrayOf(9, 9, 9)))
    }

    @Test
    fun `verifies zip integrity when no checksum is published`() {
        val installDir = tmp.resolve("install")
        val result = ReleaseInstaller().install(
            release = releaseInfo(sha = null),
            installDir = installDir,
            channel = InstallationChannel.STABLE,
        )

        assertTrue(result.installed)
        assertTrue(Files.exists(installDir.resolve("melody-sync.jar")))
    }
}
