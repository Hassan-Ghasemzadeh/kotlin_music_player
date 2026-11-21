package com.sftwr.cleandevelopment.musicplayer.music_player.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sftwr.cleandevelopment.musicplayer.domain.model.ColorPalette

@Composable
fun Music_playerTheme(
    palette: ColorPalette = ColorPalette.NEON_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = palette.accent,
        secondary = palette.surfaceCard,
        tertiary = palette.secondaryBackground,
        background = palette.primaryBackground,
        surface = palette.surfaceCard,
        onPrimary = palette.primaryBackground,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}