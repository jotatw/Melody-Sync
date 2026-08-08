package com.melodysync.scanner

import com.melodysync.metadata.MetadataFormatRegistry
import com.melodysync.model.Song

/**
 * Reads metadata for a song using the provider registered for its format.
 * On read failure the song is returned unchanged (fields left as-is).
 */
fun readMetadata(song: Song): Song =
    MetadataFormatRegistry.read(song)
