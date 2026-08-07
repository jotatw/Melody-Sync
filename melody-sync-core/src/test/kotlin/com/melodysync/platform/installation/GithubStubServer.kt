package com.melodysync.platform.installation

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.ByteArrayOutputStream

/**
 * In-process GitHub Releases stub for tests. Serves /releases JSON, a jar
 * asset and its sha256, so ReleaseClient/ReleaseInstaller can run offline.
 */
class GithubStubServer {

    val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)

    init {
        server.createContext("/releases") { exchange ->
            respond(exchange, 200, releasesJson.toByteArray(), "application/json")
        }
        server.createContext("/download.jar") { exchange ->
            respond(exchange, 200, jarBytes, "application/octet-stream")
        }
        server.createContext("/download.jar.sha256") { exchange ->
            respond(exchange, 200, "$sha256Hex  download.jar\n".toByteArray(), "text/plain")
        }
    }

    val baseUrl: String
        get() = "http://127.0.0.1:${server.address.port}"

    var releasesJson: String = "[]"

    var jarBytes: ByteArray = validZipBytes()

    val sha256Hex: String
        get() = sha256(jarBytes)

    fun start() = server.start()

    fun stop() = server.stop(0)

    private fun respond(exchange: HttpExchange, code: Int, body: ByteArray, contentType: String) {
        exchange.responseHeaders.set("Content-Type", contentType)
        exchange.sendResponseHeaders(code, body.size.toLong())
        exchange.responseBody.use { it.write(body) }
    }

    companion object {
        fun validZipBytes(): ByteArray {
            val buffer = ByteArrayOutputStream()
            ZipOutputStream(buffer).use { zip ->
                zip.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
                zip.write("Main-Class: com.melodysync.desktop.MainKt\n".toByteArray())
                zip.closeEntry()
            }
            return buffer.toByteArray()
        }

        fun sha256(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }

        fun releasesJson(vararg releases: Triple<String, Boolean, List<Pair<String, String>>>): String {
            val body = releases.joinToString(",") { (tag, prerelease, assets) ->
                val assetsJson = assets.joinToString(",") { (name, url) ->
                    """{"name":"$name","browser_download_url":"$url","size":123}"""
                }
                """{"tag_name":"$tag","prerelease":$prerelease,"assets":[$assetsJson]}"""
            }
            return "[$body]"
        }
    }
}
