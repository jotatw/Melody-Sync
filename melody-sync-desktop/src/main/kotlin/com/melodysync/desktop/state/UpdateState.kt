package com.melodysync.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melodysync.platform.installation.InstallationChannel
import com.melodysync.platform.installation.InstallationInfo
import com.melodysync.platform.installation.InstallationService
import com.melodysync.platform.installation.InstallationValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path

enum class UpdateStatus {
    IDLE,
    CHECKING,
    RUNNING,
    DONE,
    ERROR,
}

/**
 * State holder for the update / installation workflow.
 *
 * Self-contained and independent of the library, health, and organize flows.
 * State writes happen on the Compose main thread via [uiScope]; the
 * preferences and transient-message channels are delegated to the owning
 * state holder so updates keep working when they run in the background.
 */
class UpdateState(
    private val uiScope: CoroutineScope,
    private val onPrefsChanged: () -> Unit,
    private val onMessage: (String) -> Unit,
    initialChannel: InstallationChannel,
    initialAutoUpdate: Boolean,
) {
    var updateStatus by mutableStateOf(UpdateStatus.IDLE)
        private set

    var updatePhase by mutableStateOf("")
        private set

    var updateMessage by mutableStateOf<String?>(null)
        private set

    var updateAvailable by mutableStateOf(false)
        private set

    var updateSourceBased by mutableStateOf(false)
        private set

    var updateChannel by mutableStateOf(initialChannel)
        private set

    var autoUpdate by mutableStateOf(initialAutoUpdate)
        private set

    var installationInfo by mutableStateOf<InstallationInfo?>(null)
        private set

    fun selectUpdateChannel(channel: InstallationChannel) {
        updateChannel = channel
        onPrefsChanged()
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        autoUpdate = enabled
        onPrefsChanged()
    }

    fun autoUpdateIfEnabled() {
        if (!autoUpdate) return
        if (updateStatus == UpdateStatus.RUNNING || updateStatus == UpdateStatus.CHECKING) return

        val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
        if (InstallationValidator().isSourceCheckout(projectDir)) return

        uiScope.launch {
            val service = InstallationService()
            try {
                val check = withContext(Dispatchers.Default) {
                    service.checkForReleaseUpdate(channel = updateChannel)
                }
                if (check.updateAvailable && check.availableVersion != null) {
                    runUpdate(force = false)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun refreshInstallationInfo() {
        uiScope.launch {
            installationInfo = withContext(Dispatchers.Default) {
                InstallationService().detectInstallation()
            }
        }
    }

    fun checkForUpdates() {
        if (updateStatus == UpdateStatus.CHECKING || updateStatus == UpdateStatus.RUNNING) return
        updateStatus = UpdateStatus.CHECKING
        updatePhase = "Checking for updates…"
        updateMessage = null

        uiScope.launch {
            val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
            val service = InstallationService()
            val sourceCheckout = InstallationValidator().isSourceCheckout(projectDir)
            try {
                val result = withContext(Dispatchers.Default) {
                    if (sourceCheckout) {
                        service.checkForUpdate(projectDir)
                    } else {
                        service.checkForReleaseUpdate(channel = updateChannel)
                    }
                }
                val info = withContext(Dispatchers.Default) {
                    service.detectInstallation()
                }
                installationInfo = info
                updateSourceBased = sourceCheckout
                updateAvailable = result.updateAvailable
                updateMessage = when {
                    result.message != null -> result.message
                    result.updateAvailable -> {
                        val from = result.installedVersion?.let { "v$it" } ?: "nothing"
                        "Update available: $from → v${result.availableVersion}"
                    }
                    else -> "Already up to date (v${result.availableVersion})"
                }
                updateStatus = UpdateStatus.DONE
                updatePhase = "Done"
            } catch (e: Exception) {
                updateMessage = e.message ?: "Update check failed"
                updateStatus = UpdateStatus.ERROR
            }
        }
    }

    fun runUpdate(force: Boolean = true) {
        if (updateStatus == UpdateStatus.RUNNING) return
        updateStatus = UpdateStatus.RUNNING
        updatePhase = if (updateSourceBased) "Preparing build…" else "Preparing download…"
        updateMessage = null

        uiScope.launch {
            val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
            val service = InstallationService()
            try {
                val result = withContext(Dispatchers.Default) {
                    if (updateSourceBased) {
                        service.update(
                            projectDir = projectDir,
                            build = "Desktop",
                            force = force,
                            onProgress = { line ->
                                uiScope.launch { updatePhase = phaseFromLine(line) }
                            },
                        )
                    } else {
                        service.updateFromRelease(
                            channel = updateChannel,
                            build = "Desktop",
                            force = force,
                            onProgress = { line ->
                                uiScope.launch { updatePhase = phaseFromLine(line) }
                            },
                        )
                    }
                }
                installationInfo = service.detectInstallation()
                updateMessage = result.message
                updatePhase = if (result.installed) "Done" else "Failed"
                updateStatus = if (result.installed || result.sourceBased) {
                    UpdateStatus.DONE
                } else {
                    UpdateStatus.ERROR
                }
                if (result.installed) {
                    onMessage(result.message)
                }
            } catch (e: Exception) {
                updateMessage = e.message ?: "Update failed"
                updatePhase = "Failed"
                updateStatus = UpdateStatus.ERROR
            }
        }
    }

    private fun phaseFromLine(line: String): String = when {
        line.contains("Downloading", ignoreCase = true) -> "Downloading…"
        line.contains("Verifying", ignoreCase = true) -> "Verifying…"
        line.contains("Building", ignoreCase = true) -> "Compiling…"
        line.contains("Installing to", ignoreCase = true) -> "Installing…"
        line.contains("Installed!", ignoreCase = true) -> "Verifying…"
        else -> line.trim().ifBlank { "Working…" }
    }

    companion object {
        fun channelFromString(value: String): InstallationChannel =
            try {
                InstallationChannel.valueOf(value.trim().uppercase())
            } catch (_: Exception) {
                InstallationChannel.STABLE
            }
    }
}