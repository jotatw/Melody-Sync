package com.melodysync.service

/**
 * Sanitizes user-controlled strings (artist, album, title) into safe filename
 * segments, preventing path traversal and invalid names across platforms.
 *
 * See docs/architecture/SecurityAndResilienceGuide.md for the threat model.
 */
object FilenameSanitizer {

    private val FORBIDDEN_CHARS_REGEX = Regex("[/\\\\:*?\"<>|\\x00]")

    private val WINDOWS_RESERVED_NAMES = setOf(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
    )

    /**
     * Returns a safe single path segment derived from [segment], or null if the
     * result would be blank.
     */
    fun sanitize(segment: String): String? {
        val trimmed = segment.trim()
        if (trimmed.isEmpty()) return null

        var cleaned = trimmed.replace(FORBIDDEN_CHARS_REGEX, "_")

        val baseName = cleaned.substringBefore('.').uppercase()
        if (baseName in WINDOWS_RESERVED_NAMES) {
            cleaned = "Prefix_$cleaned"
        }

        while (cleaned.endsWith(".") || cleaned.endsWith(" ")) {
            cleaned = cleaned.dropLast(1)
        }

        return cleaned.ifBlank { null }
    }
}
