package com.melodysync.platform.installation

/**
 * Where an update comes from.
 *
 * [SOURCE] rebuilds from a local source checkout (the original installer).
 * [STABLE]/[BETA]/[NIGHTLY] download a published jar from GitHub Releases.
 * The default is [STABLE], which prefers a stable release but falls back to
 * the latest available when no stable release exists yet.
 */
enum class InstallationChannel {
    SOURCE,
    STABLE,
    BETA,
    NIGHTLY,
}
