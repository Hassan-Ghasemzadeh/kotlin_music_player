package com.sftwr.cleandevelopment.musicplayer.library.presentation.components.ad

import android.content.Context
import android.widget.Toast
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager

/**
 * Reusable helper function to load and show a Tapsell Rewarded Ad for unlocking premium features
 * (such as downloading songs or audio effects).
 * If the user is premium ([AdManager.isAdFree]), [onRewardUnlocked] is triggered instantly without showing an ad.
 */
fun showRewardedAdToUnlockFeature(
    context: Context,
    adManager: AdManager,
    zoneId: String = "6afc5041728e2256843b0be5",
    featureName: String = "Song Download",
    onRewardUnlocked: () -> Unit,
    onAdDismissOrError: () -> Unit = {}
) {
    val activity = context.findActivity()
    if (activity == null) {
        Toast.makeText(context, "Activity reference unavailable.", Toast.LENGTH_SHORT).show()
        onAdDismissOrError()
        return
    }

    if (adManager.isAdFree.value) {
        onRewardUnlocked()
        return
    }

    Toast.makeText(context, "Loading rewarded ad for $featureName...", Toast.LENGTH_SHORT).show()

    adManager.requestRewardedAd(
        zoneId = zoneId,
        activity = activity,
        onSuccess = { adId ->
            if (adId == "PREMIUM_BYPASS_AD_ID") {
                onRewardUnlocked()
                return@requestRewardedAd
            }

            adManager.showRewardedAd(
                adId = adId,
                activity = activity,
                onRewarded = {
                    Toast.makeText(context, "Feature Unlocked: $featureName!", Toast.LENGTH_SHORT).show()
                    onRewardUnlocked()
                },
                onError = { error ->
                    Toast.makeText(context, "Failed to show ad: $error", Toast.LENGTH_SHORT).show()
                    onAdDismissOrError()
                },
                onClosed = {
                    onAdDismissOrError()
                }
            )
        },
        onFailure = { error ->
            Toast.makeText(context, "Could not load ad: $error. Please try again later.", Toast.LENGTH_SHORT).show()
            onAdDismissOrError()
        }
    )
}
