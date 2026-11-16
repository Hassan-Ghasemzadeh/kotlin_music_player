package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffect
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SoundEffectRepository
import javax.inject.Inject

/**
 * Use case to update and persist sound effect configurations.
 */
class SaveSoundEffectConfigUseCase @Inject constructor(
    private val soundEffectRepository: SoundEffectRepository
) {
    suspend fun setEnabled(enabled: Boolean) {
        soundEffectRepository.setEnabled(enabled)
    }

    suspend fun setCurrentEffect(effect: SoundEffect) {
        soundEffectRepository.setCurrentEffect(effect)
    }

    suspend fun setVolumeOrBoost(value: Float) {
        soundEffectRepository.setVolumeOrBoost(value)
    }

    suspend fun saveFullState(state: SoundEffectState) {
        soundEffectRepository.saveSoundEffectState(state)
    }
}
