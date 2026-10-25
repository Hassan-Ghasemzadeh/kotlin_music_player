package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * An animated audio visualizer/equalizer composable utilizing Compose Canvas and infinite transitions
 * to render dynamically scaling rounded bars for playback status indication.
 */
@Composable
fun AnimatedVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 3,
    barColor: Color = Color(0xFF91D8FB)
) {
    // Create an infinite transition coordinator for running continuous equalizer animations
    val infiniteTransition = rememberInfiniteTransition(label = "EqualizerTransition")

    // Map each bar to an independent animated float value with staggered durations
    val animValues = (0 until barCount).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 300 + (index * 150),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BarAnimation_$index"
        )
    }

    // Render the visualizer bars on a low-overhead Canvas drawing surface
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calculate dynamic spacing and individual bar widths based on available canvas dimensions
        val totalSpacing = canvasWidth * 0.4f
        val availableWidthForBars = canvasWidth - totalSpacing
        val barWidth = availableWidthForBars / barCount
        val spacing = totalSpacing / (barCount - 1).coerceAtLeast(1)

        animValues.forEachIndexed { index, animValue ->
            val currentBarHeight = canvasHeight * animValue.value
            val x = index * (barWidth + spacing)
            val y = (canvasHeight - currentBarHeight) / 2

            // Draw individual rounded-corner equalizer bars centered vertically
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(width = barWidth, height = currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}