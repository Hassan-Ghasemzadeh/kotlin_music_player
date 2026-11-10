package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import javax.inject.Inject

/**
 * Use case to update and persist various audio equalizer settings.
 */
class SaveEqualizerSettingsUseCase @Inject constructor(
    private val equalizerRepository: EqualizerRepository
) {
    suspend fun setEnabled(enabled: Boolean) {
        equalizerRepository.setEnabled(enabled)
    }

    suspend fun setPreset(preset: EqualizerPreset) {
        equalizerRepository.setPreset(preset)
    }

    suspend fun setBandLevel(bandIndex: Short, levelMb: Short) {
        equalizerRepository.setBandLevel(bandIndex, levelMb)
    }

    suspend fun setBassBoost(strength: Short) {
        equalizerRepository.setBassBoostStrength(strength)
    }

    suspend fun setVirtualizer(strength: Short) {
        equalizerRepository.setVirtualizerStrength(strength)
    }

    suspend fun saveFullState(state: EqualizerState) {
        equalizerRepository.saveEqualizerState(state)
    }
}
