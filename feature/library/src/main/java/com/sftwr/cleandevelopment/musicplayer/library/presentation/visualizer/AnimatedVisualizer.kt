package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

@Composable
fun AnimatedVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 3,
    barColor: Color = Color(0xFF91D8FB)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EqualizerTransition")

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

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val totalSpacing = canvasWidth * 0.4f
        val availableWidthForBars = canvasWidth - totalSpacing
        val barWidth = availableWidthForBars / barCount
        val spacing = totalSpacing / (barCount - 1).coerceAtLeast(1)

        animValues.forEachIndexed { index, animValue ->
            val currentBarHeight = canvasHeight * animValue.value
            val x = index * (barWidth + spacing)
            val y = (canvasHeight - currentBarHeight) / 2

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(width = barWidth, height = currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}