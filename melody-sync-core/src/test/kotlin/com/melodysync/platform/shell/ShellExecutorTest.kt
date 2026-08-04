package com.melodysync.platform.shell

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShellExecutorTest {

    private val shell = ShellExecutor()

    @Test
    fun `captures stdout and exit code`() {
        val result = shell.run(listOf("bash", "-c", "echo hello"))

        assertTrue(result.succeeded)
        assertEquals(0, result.exitCode)
        assertTrue(result.stdout.contains("hello"))
        assertTrue(result.command.isNotBlank())
        assertTrue(result.durationMillis >= 0)
    }

    @Test
    fun `captures stderr separately`() {
        val result = shell.run(listOf("bash", "-c", "echo oops 1>&2"))

        assertTrue(result.stdout.isBlank() || !result.stdout.contains("oops"))
        assertTrue(result.stderr.contains("oops"))
    }

    @Test
    fun `reports non zero exit code`() {
        val result = shell.run(listOf("bash", "-c", "exit 3"))

        assertFalse(result.succeeded)
        assertEquals(3, result.exitCode)
    }

    @Test
    fun `streams stdout lines to callback`() {
        val lines = mutableListOf<String>()
        val result = shell.run(listOf("bash", "-c", "printf 'a\nb\nc\n'"), onLine = { lines.add(it) })

        assertEquals(0, result.exitCode)
        assertEquals(listOf("a", "b", "c"), lines)
    }

    @Test
    fun `returns an error result when the command cannot start`() {
        val result = shell.run(listOf("/nonexistent/binary"))

        assertEquals(-1, result.exitCode)
        assertFalse(result.succeeded)
    }
}
