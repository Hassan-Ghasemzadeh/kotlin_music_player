package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SoundEffectRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to fetch the current active sound effect configuration for audio engine application.
 */
class ApplySoundEffectUseCase @Inject constructor(
    private val soundEffectRepository: SoundEffectRepository
) {
    suspend operator fun invoke(): SoundEffectState {
        return soundEffectRepository.getSoundEffectState().first()
    }
}
