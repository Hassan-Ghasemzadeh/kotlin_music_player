package com.sftwr.cleandevelopment.musicplayer.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user premium status and ad-free preferences.
 */
interface PremiumRepository {
    /**
     * Observable stream representing whether the user has premium (ad-free) status.
     */
    val isAdFree: Flow<Boolean>

    /**
     * Updates the user's premium (ad-free) status.
     */
    suspend fun setAdFree(isAdFree: Boolean)

    /**
     * Toggles the user's premium status between active and inactive.
     */
    suspend fun togglePremium()
}
