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
import com.sftwr.cleandevelopment.musicplayer.domain.model.ColorPalette

/**
 * CompositionLocal providing whether Glassmorphic UI Style is globally enabled.
 */
val LocalGlassmorphicEnabled = compositionLocalOf { true }

/**
 * CompositionLocal providing whether Album Artwork Display is globally enabled.
 */
val LocalAlbumArtworkEnabled = compositionLocalOf { true }

/**
 * CompositionLocal providing the active global [ColorPalette] theme.
 */
val LocalColorPalette = compositionLocalOf { ColorPalette.NEON_DARK }

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
    val colorPalette = LocalColorPalette.current

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
            .background(colorPalette.secondaryBackground)
            .border(
                width = 1.dp,
                color = colorPalette.accent.copy(alpha = 0.2f),
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