package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppSettings
import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import kotlinx.coroutines.flow.Flow

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset

/**
 * Repository interface for persisting and observing app settings and preferences.
 */
interface SettingsRepository {
    /**
     * Retrieves reactive stream of [AppSettings].
     */
    fun getSettings(): Flow<AppSettings>

    suspend fun updateLanguage(language: AppLanguage)
    suspend fun updateGlassmorphicStyle(enabled: Boolean)
    suspend fun updateAlbumArtworkEnabled(enabled: Boolean)
    suspend fun updateShowAlbumArt(enabled: Boolean)

    suspend fun updatePlaybackSpeed(speed: Float)
    suspend fun updatePlaybackPitch(pitch: Float)
    suspend fun updateCrossfadeDuration(seconds: Int)
    suspend fun updateSkipSilence(enabled: Boolean)
    suspend fun updateDefaultEqualizerPreset(preset: EqualizerPreset)
    suspend fun updateDefaultEqualizerPreset(preset: String)
    suspend fun updateCircularVisualizerType(typeKey: String)
    suspend fun updateReplayGainMode(mode: ReplayGainMode)

    suspend fun addExcludedFolder(path: String)
    suspend fun removeExcludedFolder(path: String)
    suspend fun updateMinTrackDuration(seconds: Int)

    suspend fun updateLockscreenControls(enabled: Boolean)
    suspend fun updateScreenOffPlayback(enabled: Boolean)
    suspend fun updateHeadsetControls(enabled: Boolean)
    suspend fun updateAllowExternalDevicePlayback(enabled: Boolean)
    suspend fun updateShakeToSkip(enabled: Boolean)
    suspend fun updateSwipeArtworkToSkip(enabled: Boolean)
}
