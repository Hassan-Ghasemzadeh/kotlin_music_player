package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case to update and persist various audio equalizer settings.
 */
class SaveEqualizerSettingsUseCase @Inject constructor(
    private val equalizerRepository: EqualizerRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun setEnabled(enabled: Boolean) {
        equalizerRepository.setEnabled(enabled)
        if (!enabled) {
            settingsRepository.updateDefaultEqualizerPreset(EqualizerPreset.OFF)
        }
    }

    suspend fun setPreset(preset: EqualizerPreset) {
        equalizerRepository.setPreset(preset)
        if (preset == EqualizerPreset.OFF) {
            equalizerRepository.setEnabled(false)
            settingsRepository.updateDefaultEqualizerPreset(EqualizerPreset.OFF)
        } else {
            equalizerRepository.setEnabled(true)
            settingsRepository.updateDefaultEqualizerPreset(preset)
        }
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
        if (!state.isEnabled) {
            settingsRepository.updateDefaultEqualizerPreset(EqualizerPreset.OFF)
        } else {
            val preset = EqualizerPreset.fromId(state.currentPresetId)
            settingsRepository.updateDefaultEqualizerPreset(preset)
        }
    }
}
