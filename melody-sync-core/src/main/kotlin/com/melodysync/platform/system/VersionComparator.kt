package com.melodysync.platform.system

/**
 * Compares version strings such as "0.13.0", "0.13.0-dev", "v1.2.3".
 * Numeric segments are compared first; a stable build is treated as newer
 * than a dev/beta build of the same number (e.g. 0.13.0 > 0.13.0-dev).
 */
object VersionComparator {

    fun isNewer(candidate: String?, current: String?): Boolean {
        if (candidate.isNullOrBlank()) return false
        if (current.isNullOrBlank()) return true

        val a = candidate.removePrefix("v")
        val b = current.removePrefix("v")

        val aParts = numericParts(a)
        val bParts = numericParts(b)
        val maxSize = maxOf(aParts.size, bParts.size)
        for (i in 0 until maxSize) {
            val x = aParts.getOrElse(i) { 0 }
            val y = bParts.getOrElse(i) { 0 }
            if (x != y) return x > y
        }

        val aStable = !hasSuffix(a)
        val bStable = !hasSuffix(b)
        if (aStable != bStable) return aStable
        return false
    }

    private fun numericParts(version: String): List<Int> =
        version.split('.').map { segment ->
            segment.takeWhile(Char::isDigit).toIntOrNull() ?: 0
        }

    private fun hasSuffix(version: String): Boolean =
        version.any { !it.isDigit() && it != '.' }
}
