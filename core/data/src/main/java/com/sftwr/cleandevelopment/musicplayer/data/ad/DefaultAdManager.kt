package com.sftwr.cleandevelopment.musicplayer.data.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PremiumRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DefaultAdManager"

/**
 * Production implementation of [AdManager] integrating Tapsell Mediation SDK.
 * Handles initialization listeners, GDPR user consent, request validation, and main thread dispatching.
 */
@Singleton
class DefaultAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val premiumRepository: PremiumRepository
) : AdManager {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _isAdFree = MutableStateFlow(false)
    override val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        scope.launch {
            premiumRepository.isAdFree.collect { adFree ->
                _isAdFree.value = adFree
                Log.d(TAG, "Ad-Free preference updated: $adFree")
            }
        }
    }

    override fun initialize() {
        if (_isInitialized.value) return
        try {
            Tapsell.setInitializationListener {
                Log.d(TAG, "Tapsell Mediation Initialization callback received.")
                _isInitialized.value = true
            }
            Tapsell.initialize(context)
            Log.d(TAG, "Tapsell.initialize(context) called.")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Tapsell Mediation SDK: ${e.message}", e)
        }
    }

    override fun setUserConsent(activity: Activity, userConsent: Boolean) {
        try {
            Tapsell.setUserConsent(activity, userConsent)
            Log.d(TAG, "Tapsell.setUserConsent(activity, $userConsent) called successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting user consent with activity: ${e.message}", e)
            try {
                Tapsell.setUserConsent(userConsent)
                Log.d(TAG, "Tapsell.setUserConsent($userConsent) fallback called.")
            } catch (ex: Exception) {
                Log.e(TAG, "Error setting user consent fallback: ${ex.message}", ex)
            }
        }
    }

    override fun setAdFree(isAdFree: Boolean) {
        scope.launch {
            premiumRepository.setAdFree(isAdFree)
        }
    }

    private suspend fun ensureSdkInitialized() {
        if (!_isInitialized.value) {
            Log.d(TAG, "Ensuring Tapsell SDK initialization...")
            initialize()
            // Wait up to 3 seconds for initialization callback, but never hang indefinitely
            withTimeoutOrNull(3000) {
                _isInitialized.first { it }
            }
            _isInitialized.value = true
        }
    }

    override fun requestBannerAd(
        zoneId: String,
        activity: Activity?,
        onSuccess: (adId: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        if (_isAdFree.value) {
            Log.d(TAG, "User is premium. Bypassing requestBannerAd for zone: $zoneId")
            scope.launch(Dispatchers.Main) { onFailure("User is premium (ad-free)") }
            return
        }

        if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
            Log.w(TAG, "Activity is finishing or destroyed. Aborting banner ad request.")
            scope.launch(Dispatchers.Main) { onFailure("Activity is finishing or destroyed") }
            return
        }

        if (activity != null) {
            setUserConsent(activity, true)
        }

        scope.launch(Dispatchers.Main) {
            ensureSdkInitialized()

            try {
                Tapsell.requestBannerAd(
                    zoneId,
                    BannerSize.BANNER_320_50,
                    activity,
                    object : RequestResultListener {
                        override fun onSuccess(adId: String) {
                            Log.d(TAG, "Banner Ad loaded successfully. adId: $adId")
                            scope.launch(Dispatchers.Main) { onSuccess(adId) }
                        }

                        override fun onFailure(message: String) {
                            Log.w(TAG, "Failed to load Banner Ad for zone $zoneId: $message")
                            scope.launch(Dispatchers.Main) { onFailure(message) }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception requesting banner ad: ${e.message}", e)
                val msg = e.message ?: "Exception requesting banner ad"
                scope.launch(Dispatchers.Main) { onFailure(msg) }
            }
        }
    }

    override fun requestNativeAd(
        zoneId: String,
        activity: Activity?,
        onSuccess: (adId: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        if (_isAdFree.value) {
            Log.d(TAG, "User is premium. Bypassing requestNativeAd for zone: $zoneId")
            scope.launch(Dispatchers.Main) { onFailure("User is premium (ad-free)") }
            return
        }

        if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
            Log.w(TAG, "Activity is finishing or destroyed. Aborting native ad request.")
            scope.launch(Dispatchers.Main) { onFailure("Activity is finishing or destroyed") }
            return
        }

        if (activity != null) {
            setUserConsent(activity, true)
        }

        scope.launch(Dispatchers.Main) {
            ensureSdkInitialized()

            try {
                Tapsell.requestNativeAd(
                    zoneId,
                    activity,
                    object : RequestResultListener {
                        override fun onSuccess(adId: String) {
                            Log.d(TAG, "Native Ad loaded successfully. adId: $adId")
                            scope.launch(Dispatchers.Main) { onSuccess(adId) }
                        }

                        override fun onFailure(message: String) {
                            Log.w(TAG, "Failed to load Native Ad for zone $zoneId: $message")
                            scope.launch(Dispatchers.Main) { onFailure(message) }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception requesting native ad: ${e.message}", e)
                val msg = e.message ?: "Exception requesting native ad"
                scope.launch(Dispatchers.Main) { onFailure(msg) }
            }
        }
    }

    override fun requestRewardedAd(
        zoneId: String,
        activity: Activity?,
        onSuccess: (adId: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        if (_isAdFree.value) {
            Log.d(TAG, "User is premium. Rewarded ad automatically granted for zone: $zoneId")
            scope.launch(Dispatchers.Main) { onSuccess("PREMIUM_BYPASS_AD_ID") }
            return
        }

        if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
            Log.w(TAG, "Activity is finishing or destroyed. Aborting rewarded ad request.")
            scope.launch(Dispatchers.Main) { onFailure("Activity is finishing or destroyed") }
            return
        }

        if (activity != null) {
            setUserConsent(activity, true)
        }

        scope.launch(Dispatchers.Main) {
            ensureSdkInitialized()

            try {
                Tapsell.requestRewardedAd(
                    zoneId,
                    activity,
                    object : RequestResultListener {
                        override fun onSuccess(adId: String) {
                            Log.d(TAG, "Rewarded Ad loaded successfully. adId: $adId")
                            scope.launch(Dispatchers.Main) { onSuccess(adId) }
                        }

                        override fun onFailure(message: String) {
                            Log.w(TAG, "Failed to load Rewarded Ad for zone $zoneId: $message")
                            scope.launch(Dispatchers.Main) { onFailure(message) }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception requesting rewarded ad: ${e.message}", e)
                val msg = e.message ?: "Exception requesting rewarded ad"
                scope.launch(Dispatchers.Main) { onFailure(msg) }
            }
        }
    }

    override fun showRewardedAd(
        adId: String,
        activity: Activity,
        onRewarded: () -> Unit,
        onError: (error: String) -> Unit,
        onClosed: () -> Unit
    ) {
        if (_isAdFree.value || adId == "PREMIUM_BYPASS_AD_ID") {
            Log.d(TAG, "User is premium or bypass active. Unlocking reward directly.")
            scope.launch(Dispatchers.Main) {
                onRewarded()
                onClosed()
            }
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed. Aborting showing rewarded ad.")
            scope.launch(Dispatchers.Main) { onError("Activity is finishing or destroyed") }
            return
        }

        setUserConsent(activity, true)

        try {
            Tapsell.showRewardedAd(
                adId,
                activity,
                object : AdStateListener.Rewarded {
                    override fun onRewarded() {
                        Log.d(TAG, "User earned reward for adId: $adId")
                        scope.launch(Dispatchers.Main) { onRewarded() }
                    }

                    override fun onAdClosed(completionState: AdShowCompletionState) {
                        Log.d(TAG, "Rewarded ad closed with state: $completionState")
                        scope.launch(Dispatchers.Main) { onClosed() }
                    }

                    override fun onAdFailed(message: String) {
                        Log.e(TAG, "Rewarded ad failed to show: $message")
                        scope.launch(Dispatchers.Main) { onError(message) }
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Rewarded ad impression logged.")
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Rewarded ad clicked.")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing rewarded ad: ${e.message}", e)
            val msg = e.message ?: "Error showing rewarded ad"
            scope.launch(Dispatchers.Main) { onError(msg) }
        }
    }

    override fun destroyBannerAd(zoneId: String) {
        try {
            Tapsell.destroyBannerAd(zoneId)
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying banner ad $zoneId: ${e.message}")
        }
    }

    override fun destroyNativeAd(zoneId: String) {
        try {
            Tapsell.destroyNativeAd(zoneId)
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying native ad $zoneId: ${e.message}")
        }
    }
}
