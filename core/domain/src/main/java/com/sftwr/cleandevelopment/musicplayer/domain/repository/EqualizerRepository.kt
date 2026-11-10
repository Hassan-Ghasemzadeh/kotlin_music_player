package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining persistence and state retrieval for audio equalizer configurations.
 */
interface EqualizerRepository {
    /**
     * Emits continuous updates to the active [EqualizerState].
     */
    fun getEqualizerState(): Flow<EqualizerState>

    /**
     * Persists a full [EqualizerState] to storage.
     */
    suspend fun saveEqualizerState(state: EqualizerState)

    /**
     * Updates and saves the equalizer enabled toggle state.
     */
    suspend fun setEnabled(enabled: Boolean)

    /**
     * Updates and saves the active preset and maps band levels accordingly.
     */
    suspend fun setPreset(preset: EqualizerPreset)

    /**
     * Updates and saves a specific band's gain level in millibels.
     */
    suspend fun setBandLevel(bandIndex: Short, levelMb: Short)

    /**
     * Updates and saves the Bass Boost strength (0..1000).
     */
    suspend fun setBassBoostStrength(strength: Short)

    /**
     * Updates and saves the Virtualizer strength (0..1000).
     */
    suspend fun setVirtualizerStrength(strength: Short)
}
