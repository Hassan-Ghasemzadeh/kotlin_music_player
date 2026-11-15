package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ThemePickerRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThemePickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ThemePickerScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onApplyPalette = { palette ->
            viewModel.applyPalette(palette) {
                onBackClick()
            }
        },
        modifier = modifier
    )
}
