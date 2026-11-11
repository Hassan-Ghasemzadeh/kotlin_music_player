package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case to update audio & playback settings.
 */
class UpdateAudioSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setPlaybackSpeed(speed: Float) {
        settingsRepository.updatePlaybackSpeed(speed)
    }

    suspend fun setPlaybackPitch(pitch: Float) {
        settingsRepository.updatePlaybackPitch(pitch)
    }

    suspend fun setCrossfadeDuration(seconds: Int) {
        settingsRepository.updateCrossfadeDuration(seconds)
    }

    suspend fun setSkipSilence(enabled: Boolean) {
        settingsRepository.updateSkipSilence(enabled)
    }

    suspend fun setDefaultEqualizerPreset(preset: String) {
        settingsRepository.updateDefaultEqualizerPreset(preset)
    }

    suspend fun setCircularVisualizerType(typeKey: String) {
        settingsRepository.updateCircularVisualizerType(typeKey)
    }

    suspend fun setReplayGainMode(mode: ReplayGainMode) {
        settingsRepository.updateReplayGainMode(mode)
    }
}
