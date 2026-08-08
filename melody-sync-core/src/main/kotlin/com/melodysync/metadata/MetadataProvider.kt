package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion

/**
 * A per-format metadata backend. Higher-level code resolves a provider through
 * [MetadataFormatRegistry] instead of branching on the file extension.
 *
 * [write] throws on failure so callers surface the real reason (typed write
 * errors arrive in Phase B of the metadata foundation).
 */
interface MetadataProvider {
    val id: String
    val formats: Set<String>
    val supportsWrite: Boolean
    val supportedFields: List<String>
    fun read(song: Song): Song
    fun write(song: Song, suggestion: TagSuggestion): Song
}
