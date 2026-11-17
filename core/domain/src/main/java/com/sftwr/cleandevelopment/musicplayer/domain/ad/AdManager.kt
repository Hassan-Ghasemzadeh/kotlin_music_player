package com.sftwr.cleandevelopment.musicplayer.domain.ad

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface contract for Tapsell Mediation Ad operations.
 * Enforces premium (ad-free) checks before loading any ads.
 */
interface AdManager {
    /**
     * Observable state flow indicating whether ads are bypassed for premium users.
     */
    val isAdFree: StateFlow<Boolean>

    /**
     * Observable state flow indicating whether Tapsell SDK initialization is complete.
     */
    val isInitialized: StateFlow<Boolean>

    /**
     * Initializes Tapsell Mediation SDK safely.
     */
    fun initialize()

    /**
     * Transmits GDPR user consent status to Tapsell SDK and underlying ad networks (e.g. AdMob/Wortise).
     */
    fun setUserConsent(activity: Activity, userConsent: Boolean = true)

    /**
     * Sets or toggles user premium status.
     */
    fun setAdFree(isAdFree: Boolean)

    /**
     * Requests a Tapsell Banner Ad for the specified zone ID.
     */
    fun requestBannerAd(
        zoneId: String,
        activity: Activity?,
        onSuccess: (adId: String) -> Unit,
        onFailure: (error: String) -> Unit
    )

    /**
     * Requests a Tapsell Native Ad for the specified zone ID.
     */
    fun requestNativeAd(
        zoneId: String,
        activity: Activity?,
        onSuccess: (adId: String) -> Unit,
        onFailure: (error: String) -> Unit
    )

    /**
     * Requests a Tapsell Rewarded Ad for the specified zone ID.
     */
    fun requestRewardedAd(
        zoneId: String,
        activity: Activity?,
        onSuccess: (adId: String) -> Unit,
        onFailure: (error: String) -> Unit
    )

    /**
     * Shows a previously loaded Tapsell Rewarded Ad using its loaded [adId].
     */
    fun showRewardedAd(
        adId: String,
        activity: Activity,
        onRewarded: () -> Unit,
        onError: (error: String) -> Unit,
        onClosed: () -> Unit = {}
    )

    /**
     * Destroys an active Tapsell Banner Ad instance.
     */
    fun destroyBannerAd(zoneId: String)

    /**
     * Destroys an active Tapsell Native Ad instance.
     */
    fun destroyNativeAd(zoneId: String)
}
