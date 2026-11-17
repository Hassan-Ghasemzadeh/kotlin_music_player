package com.sftwr.cleandevelopment.musicplayer.library.presentation.components.ad

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import ir.tapsell.mediation.ad.views.ntv.NativeAdView
import ir.tapsell.mediation.ad.views.ntv.NativeAdViewContainer
import androidx.core.graphics.toColorInt

private const val TAG = "NativeAdCard"

/**
 * Reusable Composable wrapper for Tapsell Native Ads, rendered as a glassmorphic list card.
 * Handles automatic lifecycle cleanup and skips rendering if user is premium ([AdManager.isAdFree]).
 */
@Composable
fun NativeAdCard(
    adManager: AdManager,
    modifier: Modifier = Modifier,
    zoneId: String = "6afc4fc96cbc4169d6c3847c" // Default test zone ID
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
        adManager.requestNativeAd(
            zoneId = zoneId,
            activity = activity,
            onSuccess = { adId ->
                loadedAdId = adId
                isLoadFailed = false
            },
            onFailure = { error ->
                Log.w(TAG, "Native Ad failed to load: $error")
                isLoadFailed = true
            }
        )

        onDispose {
            val adIdToDestroy = currentAdIdState.value ?: zoneId
            adManager.destroyNativeAd(adIdToDestroy)
        }
    }

    if (!isLoadFailed && loadedAdId != null) {
        val activeAdId = loadedAdId ?: return

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val density = ctx.resources.displayMetrics.density

                    // Outer NativeAdViewContainer with dark glassmorphic rounded background
                    val container = NativeAdViewContainer(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        val backgroundDrawable = GradientDrawable().apply {
                            setColor("#261B1238".toColorInt())
                            setStroke((1 * density).toInt(), "#33FFFFFF".toColorInt())
                            cornerRadius = 16 * density
                        }
                        background = backgroundDrawable
                        setPadding(
                            (12 * density).toInt(),
                            (12 * density).toInt(),
                            (12 * density).toInt(),
                            (12 * density).toInt()
                        )
                    }

                    val mainLayout = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    // Header Row (Logo + Sponsored + Title)
                    val headerRow = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val logoView = ImageView(ctx).apply {
                        val size = (42 * density).toInt()
                        layoutParams = LinearLayout.LayoutParams(size, size).apply {
                            rightMargin = (10 * density).toInt()
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    val textColumn = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val sponsoredView = TextView(ctx).apply {
                        text = "ADVERTISEMENT"
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                        setTextColor("#00E676".toColorInt())
                    }

                    val titleView = TextView(ctx).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                        setTextColor(AndroidColor.WHITE)
                        maxLines = 1
                    }

                    textColumn.addView(sponsoredView)
                    textColumn.addView(titleView)

                    headerRow.addView(logoView)
                    headerRow.addView(textColumn)

                    // Description text
                    val descView = TextView(ctx).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                        setTextColor("#B0FFFFFF".toColorInt())
                        maxLines = 2
                        setPadding(0, (6 * density).toInt(), 0, (6 * density).toInt())
                    }

                    // Media FrameLayout (Image / Video)
                    val mediaView = FrameLayout(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (160 * density).toInt()
                        ).apply {
                            topMargin = (6 * density).toInt()
                            bottomMargin = (8 * density).toInt()
                        }
                    }

                    // Call To Action (CTA) Button
                    val ctaButton = Button(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (42 * density).toInt()
                        )
                        val btnDrawable = GradientDrawable().apply {
                            setColor("#7C4DFF".toColorInt())
                            cornerRadius = 12 * density
                        }
                        background = btnDrawable
                        setTextColor(AndroidColor.WHITE)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    }

                    mainLayout.addView(headerRow)
                    mainLayout.addView(descView)
                    mainLayout.addView(mediaView)
                    mainLayout.addView(ctaButton)

                    container.addView(mainLayout)

                    try {
                        val nativeAdView = NativeAdView.Builder(container)
                            .withTitle(titleView)
                            .withDescription(descView)
                            .withLogo(logoView)
                            .withMedia(mediaView)
                            .withCtaButton(ctaButton)
                            .withSponsored(sponsoredView)
                            .build()

                        val activity = ctx.findActivity()
                        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                            Tapsell.showNativeAd(activeAdId, nativeAdView, activity)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error building or showing native ad: ${e.message}", e)
                    }

                    container
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
