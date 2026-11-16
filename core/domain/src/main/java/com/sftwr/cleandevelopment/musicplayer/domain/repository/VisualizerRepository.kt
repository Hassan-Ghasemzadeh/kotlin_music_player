package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.VisualizerState
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining persistence and configuration management for audio visualizers.
 */
interface VisualizerRepository {
    fun getVisualizerState(): Flow<VisualizerState>
    suspend fun setSelectedVisualizer(style: VisualizerStyle)
    suspend fun setVisualizerEnabled(enabled: Boolean)
    suspend fun setAudioSensitivity(sensitivity: Float)
}
