package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Replay Gain volume normalization modes.
 */
enum class ReplayGainMode(val key: String) {
    OFF("off"),
    TRACK("track"),
    ALBUM("album");

    companion object {
        fun fromKey(key: String): ReplayGainMode {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: OFF
        }
    }
}

/**
 * Domain model representing the full application settings state.
 */
data class AppSettings(
    // Appearance & Language
    val language: AppLanguage = AppLanguage.ENGLISH,
    val glassmorphicStyleEnabled: Boolean = true,
    val isAlbumArtworkEnabled: Boolean = true,

    // Audio & Playback Effects
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f,
    val crossfadeDurationSeconds: Int = 0,
    val skipSilenceEnabled: Boolean = false,
    val defaultEqualizerPreset: EqualizerPreset = EqualizerPreset.NORMAL,
    val selectedVisualizer: String = "dynamic_scale",
    val circularVisualizerType: String = "NEON_FREQUENCY_BARS",
    val replayGainMode: ReplayGainMode = ReplayGainMode.OFF,

    // Library & Storage Management
    val excludedFolders: Set<String> = emptySet(),
    val minTrackDurationSeconds: Int = 30,

    // Controls & Background Services
    val lockscreenControlsEnabled: Boolean = true,
    val screenOffPlaybackEnabled: Boolean = true,
    val headsetControlsEnabled: Boolean = true,
    val allowExternalDevicePlayback: Boolean = true,
    val shakeToSkipEnabled: Boolean = false
) {
    val isGlassmorphicEnabled: Boolean
        get() = glassmorphicStyleEnabled

    val showAlbumArt: Boolean
        get() = isAlbumArtworkEnabled
}
