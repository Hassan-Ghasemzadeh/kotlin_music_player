package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SoundEffectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve a reactive stream of the active [SoundEffectState].
 */
class GetSoundEffectsUseCase @Inject constructor(
    private val soundEffectRepository: SoundEffectRepository
) {
    operator fun invoke(): Flow<SoundEffectState> {
        return soundEffectRepository.getSoundEffectState()
    }
}
