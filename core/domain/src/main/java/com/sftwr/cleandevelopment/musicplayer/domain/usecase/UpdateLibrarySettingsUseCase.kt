package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case to update music library & storage settings.
 */
class UpdateLibrarySettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun addExcludedFolder(path: String) {
        settingsRepository.addExcludedFolder(path)
    }

    suspend fun removeExcludedFolder(path: String) {
        settingsRepository.removeExcludedFolder(path)
    }

    suspend fun setMinTrackDuration(seconds: Int) {
        settingsRepository.updateMinTrackDuration(seconds)
    }
}
