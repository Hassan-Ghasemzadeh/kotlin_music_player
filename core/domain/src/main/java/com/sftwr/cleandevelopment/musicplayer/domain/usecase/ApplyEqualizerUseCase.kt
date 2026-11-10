package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Interface for audio effect engines capable of applying equalizer settings to active audio sessions.
 */
fun interface AudioEffectEngine {
    fun applyEqualizerSettings(audioSessionId: Int, state: EqualizerState)
}

/**
 * Use case to load and apply equalizer settings onto an active audio session ID.
 */
class ApplyEqualizerUseCase @Inject constructor(
    private val equalizerRepository: EqualizerRepository
) {
    suspend operator fun invoke(audioSessionId: Int, engine: AudioEffectEngine) {
        if (audioSessionId <= 0) return
        val state = equalizerRepository.getEqualizerState().first()
        engine.applyEqualizerSettings(audioSessionId, state)
    }

    operator fun invoke(audioSessionId: Int, state: EqualizerState, engine: AudioEffectEngine) {
        if (audioSessionId <= 0) return
        engine.applyEqualizerSettings(audioSessionId, state)
    }
}
