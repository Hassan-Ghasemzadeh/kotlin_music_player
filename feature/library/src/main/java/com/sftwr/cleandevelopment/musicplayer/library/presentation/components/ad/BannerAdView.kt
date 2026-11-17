package com.sftwr.cleandevelopment.musicplayer.library.presentation.components.ad

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.views.banner.BannerContainer

private const val TAG = "BannerAdView"

/**
 * Reusable Composable wrapper using [AndroidView] to host Tapsell Banner Ads safely.
 * Manages lifecycle disposal to prevent memory leaks and checks [AdManager.isAdFree].
 */
@Composable
fun BannerAdView(
    adManager: AdManager,
    modifier: Modifier = Modifier,
    zoneId: String = "6afc4ef54f21b435f8868231"
) {
    val isAdFree by adManager.isAdFree.collectAsStateWithLifecycle()
    if (isAdFree) {
        return
    }

    val context = LocalContext.current
    var loadedAdId by remember { mutableStateOf<String?>(null) }
    var isLoadFailed by remember { mutableStateOf(false) }
    val currentAdIdState = rememberUpdatedState(loadedAdId)

    DisposableEffect(zoneId) {
        val activity = context.findActivity()
        adManager.requestBannerAd(
            zoneId = zoneId,
            activity = activity,
            onSuccess = { adId ->
                loadedAdId = adId
                isLoadFailed = false
            },
            onFailure = { error ->
                Log.w(TAG, "Banner Ad failed to load: $error")
                isLoadFailed = true
            }
        )

        onDispose {
            val adIdToDestroy = currentAdIdState.value ?: zoneId
            adManager.destroyBannerAd(adIdToDestroy)
        }
    }

    if (!isLoadFailed && loadedAdId != null) {
        val activeAdId = loadedAdId ?: return
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    BannerContainer(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val activity = ctx.findActivity()
                        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                            try {
                                Tapsell.showBannerAd(activeAdId, this, activity)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error showing banner ad: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

internal fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
