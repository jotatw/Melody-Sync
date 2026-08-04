package com.melodysync.platform.shell

/**
 * Result of an executed shell command, kept small and diagnostic-friendly
 * so logs and the installation layer can inspect what ran and for how long.
 */
data class CommandResult(
    val command: String,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMillis: Long,
) {
    val succeeded: Boolean
        get() = exitCode == 0
}
