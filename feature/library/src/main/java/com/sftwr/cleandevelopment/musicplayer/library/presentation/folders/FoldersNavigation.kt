package com.sftwr.cleandevelopment.musicplayer.library.presentation.folders

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering the Folders screen destination in NavGraphBuilder.
 */
fun NavGraphBuilder.foldersGraph(
    onFolderClick: (String, String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    composable<Screen.Folders> {
        FoldersRoute(
            onFolderClick = onFolderClick,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick
        )
    }
}
