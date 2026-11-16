package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffect
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining persistence and state management for sound effects.
 */
interface SoundEffectRepository {
    fun getSoundEffectState(): Flow<SoundEffectState>
    suspend fun saveSoundEffectState(state: SoundEffectState)
    suspend fun setEnabled(enabled: Boolean)
    suspend fun setCurrentEffect(effect: SoundEffect)
    suspend fun setVolumeOrBoost(value: Float)
}
