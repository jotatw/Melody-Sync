package com.melodysync.metadata

import com.melodysync.model.Song

/**
 * Typed reason for a failed metadata write (Phase B of the metadata
 * foundation). Callers render [userMessage] instead of parsing library
 * exception strings.
 */
sealed class TagWriteError {

    /** The format has no write support at all. */
    data object Unsupported : TagWriteError()

    /** The file could not be parsed or written in its current form. */
    data class Parse(val reason: String) : TagWriteError()

    /** A generic filesystem/IO failure. */
    data class Io(val reason: String) : TagWriteError()

    /** The file is locked or otherwise unavailable. */
    data object Locked : TagWriteError()

    /** The file does not exist. */
    data class NotFound(val path: String) : TagWriteError()

    /** The process lacks permission to modify the file. */
    data class Permission(val path: String) : TagWriteError()

    val userMessage: String
        get() = when (this) {
            Unsupported -> "the format is not supported for writing."
            is Parse -> "metadata could not be parsed ($reason)."
            is Io -> "a file operation failed ($reason)."
            Locked -> "the file is locked or unavailable."
            is NotFound -> "the file was not found ($path)."
            is Permission -> "permission was denied ($path)."
        }
}

/**
 * Outcome of a metadata write. Exactly one of [updated] or [error] is set.
 */
data class WriteResult(
    val updated: Song? = null,
    val error: TagWriteError? = null,
) {
    val success: Boolean
        get() = updated != null
}
