package com.melodysync.platform.installation

/**
 * A downloadable release and the artifact Melody Sync should install from it.
 */
data class ReleaseInfo(
    val tag: String,
    val version: String,
    val prerelease: Boolean,
    val jarUrl: String,
    val sha256: String?,
    val sizeBytes: Long,
)
