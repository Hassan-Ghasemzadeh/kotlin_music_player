package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.SleepTimerState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Sleep Timer persistence and background task scheduling.
 */
interface SleepTimerRepository {
    /**
     * Returns a reactive flow emitting the real-time state of the Sleep Timer.
     */
    fun getSleepTimerState(): Flow<SleepTimerState>

    /**
     * Sets a sleep timer for the given duration in minutes.
     * Persists state to DataStore and enqueues a background WorkManager task.
     */
    suspend fun setSleepTimer(durationMinutes: Int)

    /**
     * Cancels any active sleep timer, clearing persisted DataStore state and WorkManager task.
     */
    suspend fun cancelSleepTimer()

    /**
     * Computes the remaining time in milliseconds until timer expiration.
     */
    suspend fun getRemainingTimeMs(): Long
}
