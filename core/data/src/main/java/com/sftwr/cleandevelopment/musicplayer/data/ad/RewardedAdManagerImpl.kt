package com.sftwr.cleandevelopment.musicplayer.data.ad

import android.app.Activity
import android.util.Log
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import com.sftwr.cleandevelopment.musicplayer.domain.ad.RewardedAdManager
import com.sftwr.cleandevelopment.musicplayer.domain.ad.RewardedAdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RewardedAdManagerImpl"

/**
 * Implementation of [RewardedAdManager] providing reactive StateFlow states for Tapsell Rewarded Ads.
 */
@Singleton
class RewardedAdManagerImpl @Inject constructor(
    private val adManager: AdManager
) : RewardedAdManager {

    private val _adState = MutableStateFlow<RewardedAdState>(RewardedAdState.Idle)
    override val adState: StateFlow<RewardedAdState> = _adState.asStateFlow()

    override fun loadAd(zoneId: String, activity: Activity?, onLoaded: (Boolean) -> Unit) {
        if (adManager.isAdFree.value) {
            _adState.value = RewardedAdState.Loaded("PREMIUM_BYPASS")
            onLoaded(true)
            return
        }

        _adState.value = RewardedAdState.Loading

        try {
            adManager.requestRewardedAd(
                zoneId = zoneId,
                activity = activity,
                onSuccess = { adId ->
                    Log.d(TAG, "Rewarded ad loaded successfully. adId: $adId")
                    _adState.value = RewardedAdState.Loaded(adId)
                    onLoaded(true)
                },
                onFailure = { error ->
                    Log.w(TAG, "Failed to load rewarded ad: $error")
                    _adState.value = RewardedAdState.Error(error)
                    onLoaded(false)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading rewarded ad: ${e.message}", e)
            _adState.value = RewardedAdState.Error(e.message ?: "Unknown error")
            onLoaded(false)
        }
    }

    override fun showAd(
        activity: Activity,
        zoneId: String,
        onRewarded: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (adManager.isAdFree.value) {
            _adState.value = RewardedAdState.RewardGranted
            onRewarded()
            return
        }

        val currentState = _adState.value
        if (currentState is RewardedAdState.Loaded) {
            val loadedAdId = currentState.adId
            adManager.showRewardedAd(
                adId = loadedAdId,
                activity = activity,
                onRewarded = {
                    _adState.value = RewardedAdState.RewardGranted
                    onRewarded()
                },
                onError = { error ->
                    Log.e(TAG, "Error showing rewarded ad: $error")
                    _adState.value = RewardedAdState.Error(error)
                    onError(error)
                },
                onClosed = {
                    if (_adState.value !is RewardedAdState.RewardGranted) {
                        _adState.value = RewardedAdState.Idle
                    }
                }
            )
        } else {
            loadAd(zoneId, activity) { success ->
                if (success) {
                    val adId = (_adState.value as? RewardedAdState.Loaded)?.adId ?: return@loadAd
                    adManager.showRewardedAd(
                        adId = adId,
                        activity = activity,
                        onRewarded = {
                            _adState.value = RewardedAdState.RewardGranted
                            onRewarded()
                        },
                        onError = { error ->
                            Log.e(TAG, "Error showing rewarded ad: $error")
                            _adState.value = RewardedAdState.Error(error)
                            onError(error)
                        },
                        onClosed = {
                            if (_adState.value !is RewardedAdState.RewardGranted) {
                                _adState.value = RewardedAdState.Idle
                            }
                        }
                    )
                } else {
                    val err = (_adState.value as? RewardedAdState.Error)?.message ?: "Failed to load rewarded ad"
                    onError(err)
                }
            }
        }
    }

    override fun resetState() {
        _adState.value = RewardedAdState.Idle
    }
}
