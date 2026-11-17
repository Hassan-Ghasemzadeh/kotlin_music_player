package com.sftwr.cleandevelopment.musicplayer.domain.ad

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * UI State for tracking Rewarded Ad loading and showing events.
 */
sealed class RewardedAdState {
    data object Idle : RewardedAdState()
    data object Loading : RewardedAdState()
    data class Loaded(val adId: String) : RewardedAdState()
    data class Error(val message: String) : RewardedAdState()
    data object RewardGranted : RewardedAdState()
}

/**
 * Interface contract for managing Tapsell Rewarded Ads using Coroutines & StateFlow.
 */
interface RewardedAdManager {
    val adState: StateFlow<RewardedAdState>

    fun loadAd(
        zoneId: String,
        activity: Activity?,
        onLoaded: (Boolean) -> Unit = {}
    )

    fun showAd(
        activity: Activity,
        zoneId: String,
        onRewarded: () -> Unit,
        onError: (String) -> Unit = {}
    )

    fun resetState()
}
