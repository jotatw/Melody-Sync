package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion

/**
 * Resolves the [MetadataProvider] for a file extension. Higher-level code
 * asks this registry instead of branching on the extension.
 */
object MetadataFormatRegistry {

    private val providers: List<MetadataProvider> = listOf(JAudioTaggerProvider, OpusProvider)

    private val byExtension: Map<String, MetadataProvider> =
        providers.flatMap { provider ->
            provider.formats.map { format -> format to provider }
        }.toMap()

    fun providerFor(extension: String): MetadataProvider? =
        byExtension[extension.lowercase()]

    fun read(song: Song): Song =
        providerFor(song.extension)?.read(song) ?: song

    fun write(song: Song, suggestion: TagSuggestion): WriteResult {
        val provider = providerFor(song.extension)
            ?: return WriteResult(error = TagWriteError.Unsupported)
        if (!provider.supportsWrite(song.extension)) {
            return WriteResult(error = TagWriteError.Unsupported)
        }
        return provider.write(song, suggestion)
    }
}
