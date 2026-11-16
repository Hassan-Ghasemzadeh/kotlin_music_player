package com.sftwr.cleandevelopment.musicplayer.domain.model

import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle

/**
 * Domain model representing the visualizer state and active style configuration.
 *
 * @property selectedStyle Currently active [VisualizerStyle].
 * @property isEnabled Whether visualizer rendering is enabled.
 * @property audioSensitivity Sensitivity multiplier applied to audio spectrum inputs.
 */
data class VisualizerState(
    val selectedStyle: VisualizerStyle = VisualizerStyle.FANTASY_PLANET,
    val isEnabled: Boolean = true,
    val audioSensitivity: Float = 1.0f
)
