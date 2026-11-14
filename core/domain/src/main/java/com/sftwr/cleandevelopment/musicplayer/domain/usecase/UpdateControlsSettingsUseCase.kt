package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case to update playback control & service settings.
 */
class UpdateControlsSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setLockscreenControls(enabled: Boolean) {
        settingsRepository.updateLockscreenControls(enabled)
    }

    suspend fun setScreenOffPlayback(enabled: Boolean) {
        settingsRepository.updateScreenOffPlayback(enabled)
    }

    suspend fun setHeadsetControls(enabled: Boolean) {
        settingsRepository.updateHeadsetControls(enabled)
    }

    suspend fun setAllowExternalDevicePlayback(enabled: Boolean) {
        settingsRepository.updateAllowExternalDevicePlayback(enabled)
    }

    suspend fun setShakeToSkip(enabled: Boolean) {
        settingsRepository.updateShakeToSkip(enabled)
    }

    suspend fun setSwipeArtworkToSkip(enabled: Boolean) {
        settingsRepository.updateSwipeArtworkToSkip(enabled)
    }
}
