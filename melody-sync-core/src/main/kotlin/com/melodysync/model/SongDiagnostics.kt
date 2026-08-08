package com.melodysync.model

/**
 * A proposed tag edit. Only non-null fields are written; a suggestion never
 * overwrites a field the caller did not intend to change.
 */
data class TagSuggestion(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
) {
    val hasChanges: Boolean
        get() = title != null || artist != null || album != null
}

enum class MissingField {
    TITLE,
    ARTIST,
    ALBUM,
}

enum class QualityFlag {
    LOW_BITRATE,
    ZERO_DURATION,
}

/**
 * What is wrong with a single song and why. Drives the Quick-Fix HUD.
 */
data class SongDiagnostics(
    val song: Song,
    val missing: List<MissingField> = emptyList(),
    val flags: List<QualityFlag> = emptyList(),
) {
    val hasIssues: Boolean
        get() = missing.isNotEmpty() || flags.isNotEmpty()

    val missingLabel: String
        get() = missing.joinToString(", ") { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
}
