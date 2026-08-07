package com.melodysync.platform.installation

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReleaseClientTest {

    private val server = GithubStubServer()

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop()
    }

    private fun client() = ReleaseClient(
        apiBaseUrl = "${server.baseUrl}",
        httpClient = java.net.http.HttpClient.newBuilder().build(),
    )

    private fun releaseJson(tag: String, prerelease: Boolean): String {
        val jar = "${server.baseUrl}/download.jar"
        val sha = "${server.baseUrl}/download.jar.sha256"
        return GithubStubServer.releasesJson(
            Triple(tag, prerelease, listOf(
                "melody-sync-linux-x64-${tag.removePrefix("v")}.jar" to jar,
                "melody-sync-linux-x64-${tag.removePrefix("v")}.jar.sha256" to sha,
            )),
        )
    }

    @Test
    fun `stable picks the latest stable release`() {
        server.releasesJson = GithubStubServer.releasesJson(
            Triple("v0.13.0-dev", true, listOf(
                "melody-sync-linux-x64-0.13.0-dev.jar" to "${server.baseUrl}/download.jar",
            )),
            Triple("v0.12.0", false, listOf(
                "melody-sync-linux-x64-0.12.0.jar" to "${server.baseUrl}/download.jar",
            )),
        )

        val release = client().latestRelease(InstallationChannel.STABLE)

        assertEquals("0.12.0", release.version)
        assertFalse(release.prerelease)
    }

    @Test
    fun `stable falls back to prerelease when no stable exists`() {
        server.releasesJson = releaseJson("v0.13.0-dev", prerelease = true)

        val release = client().latestRelease(InstallationChannel.STABLE)

        assertEquals("0.13.0-dev", release.version)
        assertTrue(release.prerelease)
    }

    @Test
    fun `beta takes the latest release including prereleases`() {
        server.releasesJson = releaseJson("v0.13.0-dev", prerelease = true)

        val release = client().latestRelease(InstallationChannel.BETA)

        assertEquals("0.13.0-dev", release.version)
    }

    @Test
    fun `selects jar and sha256 assets`() {
        server.releasesJson = releaseJson("v0.13.0-dev", prerelease = true)

        val release = client().latestRelease(InstallationChannel.STABLE)

        assertEquals("${server.baseUrl}/download.jar", release.jarUrl)
        assertEquals(server.sha256Hex, release.sha256)
    }

    @Test
    fun `missing jar asset raises a clear error`() {
        server.releasesJson = GithubStubServer.releasesJson(
            Triple("v0.13.0-dev", true, listOf("notes.txt" to "http://x/notes.txt")),
        )

        val error = runCatching { client().latestRelease(InstallationChannel.STABLE) }.exceptionOrNull()

        assertTrue(error != null)
        assertTrue(error!!.message!!.contains("jar asset"))
    }

    @Test
    fun `no releases raises a clear error`() {
        val error = runCatching { client().latestRelease(InstallationChannel.STABLE) }.exceptionOrNull()

        assertTrue(error != null)
        assertTrue(error!!.message!!.contains("No releases"))
    }
}
