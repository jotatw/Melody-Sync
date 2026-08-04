package com.melodysync.platform.system

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VersionInfoTest {

    @Test
    fun `version is present and display version has v prefix`() {
        assertTrue(VersionInfo.version.isNotBlank())
        assertTrue(VersionInfo.displayVersion.startsWith("v"))
    }

    @Test
    fun `display version does not double the v prefix`() {
        val info = VersionInfo
        assertTrue(!info.displayVersion.startsWith("vv"))
    }
}
