package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion

/**
 * A per-format metadata backend. Higher-level code resolves a provider through
 * [MetadataFormatRegistry] instead of branching on the file extension.
 *
 * [write] reports failures through a typed [WriteResult] rather than throwing,
 * so the UI can explain the class of failure (see [TagWriteError]).
 */
interface MetadataProvider {
    val id: String
    val formats: Set<String>
    val supportedFields: List<String>
    fun read(song: Song): Song
    fun write(song: Song, suggestion: TagSuggestion): WriteResult

    /** Whether [write] is supported for a specific extension within [formats]. */
    fun supportsWrite(extension: String): Boolean = extension.lowercase() in formats
}
