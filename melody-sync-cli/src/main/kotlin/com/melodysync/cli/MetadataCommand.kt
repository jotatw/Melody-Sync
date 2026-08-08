package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.melodysync.scanner.MetadataDiagnosticService
import java.nio.file.Files

class MetadataCommand : CliktCommand(
    name = "metadata",
) {
    private val file by argument(
        name = "file",
        help = "Audio file to inspect for metadata read/write support",
    ).path().check("File must exist") { Files.isRegularFile(it) }

    private val writeTest by option(
        "--write-test",
        help = "Attempt a safe write on a temporary copy (never touches the original).",
    ).flag()

    override fun help(context: Context): String =
        "Inspect metadata read/write support for an audio file"

    override fun run() {
        val diagnostic = MetadataDiagnosticService.inspect(file, writeTest)

        echo("File:         ${diagnostic.file}")
        echo("Format:       ${diagnostic.format}")
        echo("Provider:     ${diagnostic.provider}")
        echo("Read:         ${if (diagnostic.readSupported) "yes" else "no"}")
        echo("Read result:  ${if (diagnostic.readOk) "ok" else "failed"}")
        diagnostic.readReason?.let { echo("Read reason:  $it") }
        echo("Write:        ${if (diagnostic.writeSupported) "yes" else "no"}")
        echo("Fields:       ${diagnostic.supportedFields.joinToString(", ")}")

        val result = diagnostic.writeTest
        if (result != null) {
            echo("Write test:   ${if (result.passed) "passed" else "failed"}")
            result.reason?.let { echo("Reason:       $it") }
        } else {
            echo("Write test:   not requested (use --write-test)")
        }
    }
}
