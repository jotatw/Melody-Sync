package com.melodysync.platform.shell

import java.nio.file.Path

/**
 * Thin wrapper around [ProcessBuilder] that captures stdout/stderr, streams
 * stdout line-by-line through [onLine] and reports a [CommandResult].
 *
 * Open for subclassing so callers can inject fakes in tests.
 */
open class ShellExecutor {

    open fun run(
        command: List<String>,
        workingDir: Path? = null,
        onLine: ((String) -> Unit)? = null,
    ): CommandResult {
        val startNanos = System.nanoTime()
        val process = try {
            val builder = ProcessBuilder(command)
            if (workingDir != null) builder.directory(workingDir.toFile())
            builder.start()
        } catch (e: Exception) {
            return CommandResult(
                command = command.joinToString(" "),
                stdout = "",
                stderr = e.message ?: "failed to start process",
                exitCode = -1,
                durationMillis = elapsedMillis(startNanos),
            )
        }

        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val stderrThread = Thread {
            try {
                process.errorStream.bufferedReader().useLines { lines ->
                    lines.forEach { stderr.appendLine(it) }
                }
            } catch (_: Exception) {
                // best effort
            }
        }.apply {
            isDaemon = true
            start()
        }

        try {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    stdout.appendLine(line)
                    onLine?.invoke(line)
                }
            }
        } catch (_: Exception) {
            // best effort
        }

        stderrThread.join(5_000)
        val exitCode = process.waitFor()

        return CommandResult(
            command = command.joinToString(" "),
            stdout = stdout.toString(),
            stderr = stderr.toString(),
            exitCode = exitCode,
            durationMillis = elapsedMillis(startNanos),
        )
    }

    private fun elapsedMillis(startNanos: Long): Long =
        (System.nanoTime() - startNanos) / 1_000_000
}
