package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

fun NavGraphBuilder.themePickerGraph(
    onBackClick: () -> Unit
) {
    composable<Screen.ThemePicker> {
        ThemePickerRoute(
            onBackClick = onBackClick
        )
    }
}
