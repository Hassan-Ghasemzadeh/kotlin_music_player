package com.sftwr.cleandevelopment.musicplayer.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal providing whether Glassmorphic UI Style is globally enabled.
 */
val LocalGlassmorphicEnabled = compositionLocalOf { true }

/**
 * CompositionLocal providing whether Album Artwork Display is globally enabled.
 */
val LocalAlbumArtworkEnabled = compositionLocalOf { true }

/**
 * A reusable Glassmorphism container card featuring a translucent gradient background
 * and a subtle glowing gradient border for modern UI design, dynamically adapting
 * to the global [LocalGlassmorphicEnabled] preference.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    val isGlassmorphicEnabled = LocalGlassmorphicEnabled.current

    val backgroundModifier = if (isGlassmorphicEnabled) {
        Modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
            .background(Color(0xFF1B1238))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = shape
            )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(backgroundModifier)
    ) {
        content()
    }
}
