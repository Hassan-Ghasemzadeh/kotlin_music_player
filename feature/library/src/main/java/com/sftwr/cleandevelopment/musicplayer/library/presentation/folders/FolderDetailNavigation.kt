package com.sftwr.cleandevelopment.musicplayer.library.presentation.folders

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering FolderDetail screen destination in NavGraphBuilder.
 */
fun NavGraphBuilder.folderDetailGraph(
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit
) {
    composable<Screen.FolderDetail> {
        FolderDetailRoute(
            playerManager = playerManager,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onInfoClick = onInfoClick,
            onTagEditClick = onTagEditClick
        )
    }
}
