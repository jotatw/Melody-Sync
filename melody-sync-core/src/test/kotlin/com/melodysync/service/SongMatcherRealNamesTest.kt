package com.melodysync.service

import com.melodysync.model.Song
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Locks the local-suggestion quality on real-world YouTube-derived filenames
 * (see Product Validation, real library 2026-08). The `Artist - Title` names
 * are recovered by the separator heuristic; the known gaps (en-dash separator,
 * underscores-as-spaces, "[ORIGINAL] X - Y" inversion, "by X" phrasing) are
 * intentionally not asserted here — they are documented in
 * `docs/planning/product-validation-report.md` as the Metadata Enrichment case.
 */
class SongMatcherRealNamesTest {

    private val dir = Path.of("/home/joao/Músicas")

    private fun suggest(name: String) =
        SongMatcher.suggest(Song(path = dir.resolve(name), size = 0))

    @Test
    fun `recovers artist from Artist - Title youtube filenames`() {
        assertEquals("Nightcore", suggest("Nightcore - HUSHH - (Lyrics)(MP3_320K).mp3").artist)
        assertEquals("PSYQUI", suggest("PSYQUI - don_t you want me ft. Such(MP3_320K).mp3").artist)
        assertEquals("Steam Phunk", suggest("Steam Phunk - Need You (ft. Apsley)(MP3_320K).mp3").artist)
        assertEquals("Varien", suggest("Varien - Born of Blood_ Risen From Ash(MP3_320K).mp3").artist)
        assertEquals("Varien", suggest("Varien - Can You Feel My Heart (feat. Andrew Zink)(MP3_320K).mp3").artist)
        assertEquals("Vosai", suggest("Vosai - Broken (ft. Ratfoot)(MP3_320K).mp3").artist)
        assertEquals("dark cat", suggest("dark cat - CRAZY MILK(MP3_320K).mp3").artist)
    }

    @Test
    fun `title keeps the artist portion when a bracketed prefix is present`() {
        // The heuristic treats "[ORIGINAL] REFLECT - Gawr Gura" as artist =
        // "[ORIGINAL] REFLECT" (the bracket prefix is part of the first half).
        // This is a known gap, not a regression: enrichment/YouTube handles it.
        assertEquals("[ORIGINAL] REFLECT", suggest("[ORIGINAL] REFLECT - Gawr Gura(MP3_320K).mp3").artist)
    }
}