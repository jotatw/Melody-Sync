package com.melodysync.platform.system

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VersionComparatorTest {

    @Test
    fun `newer numeric versions are detected`() {
        assertTrue(VersionComparator.isNewer("0.14.0", "0.13.0"))
        assertTrue(VersionComparator.isNewer("1.0.0", "0.99.0"))
        assertTrue(VersionComparator.isNewer("0.10.0", "0.9.0"))
        assertTrue(VersionComparator.isNewer("v0.14.0", "0.13.0-dev"))
    }

    @Test
    fun `stable beats dev beta rc of the same number`() {
        assertTrue(VersionComparator.isNewer("0.13.0", "0.13.0-dev"))
        assertTrue(VersionComparator.isNewer("0.13.0", "0.13.0-beta"))
        assertTrue(VersionComparator.isNewer("0.13.0", "0.13.0-rc"))
    }

    @Test
    fun `equal or older versions are not newer`() {
        assertFalse(VersionComparator.isNewer("0.13.0", "0.13.0"))
        assertFalse(VersionComparator.isNewer("0.13.0", "0.14.0"))
        assertFalse(VersionComparator.isNewer("0.13.0-dev", "0.13.0"))
        assertFalse(VersionComparator.isNewer("0.13.0-dev", "0.13.0-dev"))
    }

    @Test
    fun `missing current version means any candidate is newer`() {
        assertTrue(VersionComparator.isNewer("0.13.0", null))
        assertTrue(VersionComparator.isNewer("0.13.0", ""))
    }

    @Test
    fun `blank candidate is never newer`() {
        assertFalse(VersionComparator.isNewer("", "0.13.0"))
        assertFalse(VersionComparator.isNewer(null, "0.13.0"))
    }
}
