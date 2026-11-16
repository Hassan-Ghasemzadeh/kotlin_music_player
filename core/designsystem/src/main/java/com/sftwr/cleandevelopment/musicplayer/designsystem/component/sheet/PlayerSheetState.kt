package com.sftwr.cleandevelopment.musicplayer.designsystem.component.sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State representing the expansion state of the Now Playing player sheet.
 */
enum class PlayerSheetValue {
    Collapsed,
    Expanded
}

/**
 * High-performance state controller managing the transition progress, drag gestures,
 * and spring animation lifecycle for the Mini-Player to Full-Screen Now-Playing bottom sheet.
 *
 * Adheres strictly to Clean Architecture & SOLID principles by decoupling UI gesture state
 * from audio playback domain logic.
 */
@Stable
class PlayerSheetState(
    initialValue: PlayerSheetValue = PlayerSheetValue.Collapsed,
    val animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
) {
    /**
     * Current target value of the sheet.
     */
    var targetValue by mutableStateOf(initialValue)
        private set

    /**
     * Internal animatable tracking normalized expansion progress between 0f (Collapsed) and 1f (Expanded).
     */
    internal val progressAnimatable = Animatable(if (initialValue == PlayerSheetValue.Expanded) 1f else 0f)

    /**
     * Normalized progress value (0.0f = fully collapsed, 1.0f = fully expanded).
     */
    val progress: Float
        get() = progressAnimatable.value.coerceIn(0f, 1f)

    /**
     * Indicates whether the sheet is currently in an expanded state or actively expanding past midpoint.
     */
    val isExpanded: Boolean
        get() = targetValue == PlayerSheetValue.Expanded || progress > 0.5f

    /**
     * Indicates whether the sheet is fully collapsed.
     */
    val isCollapsed: Boolean
        get() = targetValue == PlayerSheetValue.Collapsed && progress <= 0.01f

    /**
     * Expands the player sheet to full-screen view with high-performance spring dynamics.
     */
    suspend fun expand() {
        targetValue = PlayerSheetValue.Expanded
        progressAnimatable.animateTo(1f, animationSpec)
    }

    /**
     * Collapses the player sheet back down to the mini-player bar.
     */
    suspend fun collapse() {
        targetValue = PlayerSheetValue.Collapsed
        progressAnimatable.animateTo(0f, animationSpec)
    }

    /**
     * Toggles between expanded and collapsed state.
     */
    suspend fun toggle() {
        if (isExpanded) {
            collapse()
        } else {
            expand()
        }
    }

    /**
     * Directly snaps the expansion progress to a specific normalized value.
     */
    suspend fun snapTo(targetProgress: Float) {
        progressAnimatable.snapTo(targetProgress.coerceIn(0f, 1f))
    }

    /**
     * Dispatches raw pixel drag deltas during touch swipe gestures.
     * Updates progress instantly on the GPU without triggering layout recompositions.
     *
     * @param deltaY The pixel movement along the Y axis. Negative indicates upward swipe.
     * @param maxOffsetPx Total vertical pixel distance between collapsed and expanded anchor positions.
     */
    suspend fun dispatchRawDelta(deltaY: Float, maxOffsetPx: Float) {
        if (maxOffsetPx <= 0f) return
        val currentProgress = progressAnimatable.value
        val progressDelta = -deltaY / maxOffsetPx
        val newProgress = (currentProgress + progressDelta).coerceIn(0f, 1f)
        progressAnimatable.snapTo(newProgress)
    }

    /**
     * Settles the sheet position based on drag velocity and release position threshold.
     */
    suspend fun settle(velocityPxPerSec: Float) {
        val currentP = progress
        val target = when {
            velocityPxPerSec < -500f -> PlayerSheetValue.Expanded
            velocityPxPerSec > 500f -> PlayerSheetValue.Collapsed
            currentP > 0.35f -> PlayerSheetValue.Expanded
            else -> PlayerSheetValue.Collapsed
        }
        if (target == PlayerSheetValue.Expanded) {
            expand()
        } else {
            collapse()
        }
    }
}

/**
 * Creates and remembers a [PlayerSheetState] across recompositions.
 */
@Composable
fun rememberPlayerSheetState(
    initialValue: PlayerSheetValue = PlayerSheetValue.Collapsed
): PlayerSheetState {
    return remember { PlayerSheetState(initialValue = initialValue) }
}
