package com.melodysync.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FilenameSanitizerTest {

    @Test
    fun `replaces forbidden characters with underscore`() {
        assertEquals("a_b_c_d", FilenameSanitizer.sanitize("a/b\\c:d"))
        assertEquals("a_b_c_d", FilenameSanitizer.sanitize("a*b?c\"d"))
        assertEquals("a_b_c_d", FilenameSanitizer.sanitize("a<b>c|d"))
    }

    @Test
    fun `prevents path traversal`() {
        assertEquals(".._.._.._.._etc", FilenameSanitizer.sanitize("../../../../etc"))
    }

    @Test
    fun `prefixes windows reserved names`() {
        assertEquals("Prefix_CON", FilenameSanitizer.sanitize("CON"))
        assertEquals("Prefix_AUX", FilenameSanitizer.sanitize("AUX"))
        assertEquals("Prefix_NUL", FilenameSanitizer.sanitize("NUL"))
        assertEquals("Prefix_COM1", FilenameSanitizer.sanitize("COM1"))
        assertEquals("Prefix_LPT3", FilenameSanitizer.sanitize("LPT3"))
        assertEquals("Prefix_CON.mp3", FilenameSanitizer.sanitize("CON.mp3"))
    }

    @Test
    fun `trims trailing dots and spaces`() {
        assertEquals("song", FilenameSanitizer.sanitize("song."))
        assertEquals("song", FilenameSanitizer.sanitize("song "))
        assertEquals("song", FilenameSanitizer.sanitize("song..  "))
    }

    @Test
    fun `returns null for blank input`() {
        assertNull(FilenameSanitizer.sanitize(""))
        assertNull(FilenameSanitizer.sanitize("   "))
        assertNull(FilenameSanitizer.sanitize("..."))
    }

    @Test
    fun `keeps valid names unchanged`() {
        assertEquals("Nevermind", FilenameSanitizer.sanitize("Nevermind"))
        assertEquals("Bohemian Rhapsody", FilenameSanitizer.sanitize("Bohemian Rhapsody"))
    }
}
