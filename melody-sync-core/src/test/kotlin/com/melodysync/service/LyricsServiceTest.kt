package com.melodysync.service

import com.melodysync.model.Song
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.nio.file.Path

class LyricsServiceTest {

    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    private val baseUrl: String get() = "http://127.0.0.1:${server.address.port}/v1"

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    private fun respond(status: Int, body: String) {
        server.createContext("/v1") { exchange ->
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }

    private fun song(artist: String?, title: String?) =
        Song(path = Path.of("/music/a.mp3"), size = 0, artist = artist, title = title)

    @Test
    fun `returns lyrics on a 200 response`() {
        respond(200, """{"lyrics":"line one\nline two","status":200}""")

        val lyrics = LyricsService.fetch(song("Artist", "Title"), baseUrl)

        assertEquals("line one\nline two", lyrics)
    }

    @Test
    fun `returns null when the API reports no lyrics`() {
        respond(404, """{"error":"No lyrics found"}""")

        assertNull(LyricsService.fetch(song("Artist", "Title"), baseUrl))
    }

    @Test
    fun `returns null when artist or title is missing`() {
        respond(200, """{"lyrics":"x"}""")

        assertNull(LyricsService.fetch(song(null, "Title"), baseUrl))
        assertNull(LyricsService.fetch(song("Artist", null), baseUrl))
    }

    @Test
    fun `returns null for an unparseable response`() {
        respond(200, "not json")

        assertNull(LyricsService.fetch(song("Artist", "Title"), baseUrl))
    }
}
