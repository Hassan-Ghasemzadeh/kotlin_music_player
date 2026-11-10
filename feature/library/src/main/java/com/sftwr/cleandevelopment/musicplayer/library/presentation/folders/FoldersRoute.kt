package com.sftwr.cleandevelopment.musicplayer.library.presentation.folders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful route wrapper for Folders screen.
 */
@Composable
fun FoldersRoute(
    viewModel: FoldersViewModel = hiltViewModel(),
    onFolderClick: (String, String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FoldersScreen(
        folders = uiState.folders,
        isLoading = uiState.isLoading,
        onFolderClick = onFolderClick,
        onBackClick = onBackClick,
        onSearchClick = onSearchClick
    )
}
