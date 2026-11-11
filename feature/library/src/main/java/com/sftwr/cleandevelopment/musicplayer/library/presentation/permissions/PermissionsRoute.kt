package com.sftwr.cleandevelopment.musicplayer.library.presentation.permissions

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Stateful route component hosting the PermissionsScreen.
 */
@Composable
fun PermissionsRoute(
    onBackClick: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    PermissionsScreen(
        viewModel = viewModel,
        onBackClick = onBackClick
    )
}
