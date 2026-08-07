package com.melodysync.platform.installation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Queries the GitHub Releases API for the latest Melody Sync release and
 * picks the Linux x64 uber jar (and its sha256, when published).
 *
 * Channel selection:
 * - [InstallationChannel.STABLE] prefers the latest non-prerelease, falling
 *   back to the latest release when no stable release exists yet.
 * - [InstallationChannel.BETA]/[InstallationChannel.NIGHTLY] take the latest
 *   release (including pre-releases).
 */
class ReleaseClient(
    private val repo: String = "jotatw/Melody-Sync",
    private val apiBaseUrl: String = "https://api.github.com/repos/$repo",
    private val httpClient: HttpClient = defaultClient(),
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun latestRelease(channel: InstallationChannel): ReleaseInfo {
        val releases = fetchReleases()
        if (releases.isEmpty()) {
            throw ReleaseClientException("No releases found for $repo")
        }

        val candidate = when (channel) {
            InstallationChannel.SOURCE -> error("SOURCE channel has no remote release")
            InstallationChannel.STABLE ->
                releases.firstOrNull { !it.prerelease } ?: releases.first()
            InstallationChannel.BETA, InstallationChannel.NIGHTLY -> releases.first()
        }

        val jarAsset = candidate.assets.firstOrNull { it.name.endsWith(".jar") && it.name.startsWith("melody-sync-linux-x64-") }
            ?: throw ReleaseClientException("No linux-x64 jar asset in release ${candidate.tag_name}")

        val sha256Asset = candidate.assets.firstOrNull { it.name == "${jarAsset.name}.sha256" }
        val sha256 = sha256Asset?.let {
            downloadText(it.browser_download_url).trim().split(Regex("\\s+")).firstOrNull()?.ifBlank { null }
        }

        return ReleaseInfo(
            tag = candidate.tag_name,
            version = candidate.tag_name.removePrefix("v"),
            prerelease = candidate.prerelease,
            jarUrl = jarAsset.browser_download_url,
            sha256 = sha256,
            sizeBytes = jarAsset.size,
        )
    }

    fun downloadJar(url: String, destination: java.nio.file.Path) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(10))
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() != 200) {
            throw ReleaseClientException("Download failed with HTTP ${response.statusCode()}")
        }
        try {
            java.nio.file.Files.newOutputStream(
                destination,
                java.nio.file.StandardOpenOption.CREATE_NEW,
                java.nio.file.StandardOpenOption.WRITE,
            ).use { out -> response.body().use { it.copyTo(out) } }
        } catch (e: Exception) {
            java.nio.file.Files.deleteIfExists(destination)
            throw ReleaseClientException("Download failed: ${e.message}")
        }
    }

    private fun fetchReleases(): List<GitHubRelease> {
        val body = getText("$apiBaseUrl/releases?per_page=10")
        return json.decodeFromString<List<GitHubRelease>>(body)
    }

    private fun getText(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "melody-sync")
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw ReleaseClientException("GitHub API error ${response.statusCode()}: ${response.body().take(200)}")
        }
        return response.body()
    }

    private fun downloadText(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw ReleaseClientException("Download failed with HTTP ${response.statusCode()}")
        }
        return response.body()
    }

    private companion object {
        fun defaultClient(): HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    class ReleaseClientException(message: String) : Exception(message)
}

@Serializable
private data class GitHubRelease(
    val tag_name: String,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
private data class GitHubAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long = 0,
)
