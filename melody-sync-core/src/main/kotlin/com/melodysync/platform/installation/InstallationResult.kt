package com.melodysync.platform.installation

/**
 * Outcome of an installation/update attempt.
 *
 * [sourceBased] distinguishes source installs (can rebuild) from release
 * installs (cannot). [installed]/[rebuilt] describe what actually happened.
 */
data class InstallationResult(
    val version: String,
    val installed: Boolean,
    val rebuilt: Boolean,
    val sourceBased: Boolean,
    val message: String,
)
