package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Extension function to register the home screen and its navigation graph
 * without exposing internal ViewModels or UI components to the :app module.
 */
@OptIn(InternalSerializationApi::class)
fun NavGraphBuilder.homeGraph(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onNavigateToTracks: () -> Unit,
) {
    composable<Screen.Home> {

        HomeRoute(
            onNavigateToPlayer = onNavigateToPlayer,
            onNavigateToSearch = onNavigateToSearch,
            onOpenPlaylist = onOpenPlaylist,
            onNavigateToTracks = onNavigateToTracks
        )
    }
}