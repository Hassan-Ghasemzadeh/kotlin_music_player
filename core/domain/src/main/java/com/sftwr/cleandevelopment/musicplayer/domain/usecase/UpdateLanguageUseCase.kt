package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case to update application language.
 */
class UpdateLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(language: AppLanguage) {
        settingsRepository.updateLanguage(language)
    }
}
