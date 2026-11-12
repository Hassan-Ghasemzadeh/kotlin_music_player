package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case to update UI appearance settings.
 */
class UpdateAppearanceSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setGlassmorphicStyle(enabled: Boolean) {
        settingsRepository.updateGlassmorphicStyle(enabled)
    }

    suspend fun setAlbumArtworkEnabled(enabled: Boolean) {
        settingsRepository.updateAlbumArtworkEnabled(enabled)
    }

    suspend fun setShowAlbumArt(enabled: Boolean) {
        settingsRepository.updateAlbumArtworkEnabled(enabled)
    }
}
