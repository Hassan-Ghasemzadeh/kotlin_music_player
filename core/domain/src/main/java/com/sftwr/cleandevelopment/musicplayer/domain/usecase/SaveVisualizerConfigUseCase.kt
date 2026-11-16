package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.VisualizerRepository
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import javax.inject.Inject

/**
 * Use case to save and apply audio visualizer configurations across repositories.
 */
class SaveVisualizerConfigUseCase @Inject constructor(
    private val visualizerRepository: VisualizerRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun setSelectedVisualizer(style: VisualizerStyle) {
        visualizerRepository.setSelectedVisualizer(style)
        settingsRepository.updateCircularVisualizerType(style.id)
    }

    suspend fun setVisualizerEnabled(enabled: Boolean) {
        visualizerRepository.setVisualizerEnabled(enabled)
    }

    suspend fun setAudioSensitivity(sensitivity: Float) {
        visualizerRepository.setAudioSensitivity(sensitivity)
    }
}
