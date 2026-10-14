package com.sftwr.cleandevelopment.musicplayer.home

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.home.presentation.HomeRoute
import com.sftwr.cleandevelopment.musicplayer.home.presentation.viewmodel.HomeViewModel
import kotlinx.serialization.Serializable

// Define type-safe route keys for home feature
@Serializable
data object HomeRouteKey

@Serializable
data class PlayerRouteKey(val songId: String)

/**
 * Extension function to register the home screen and its navigation graph
 * without exposing internal ViewModels or UI components to the :app module.
 */
fun NavGraphBuilder.homeGraph(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
) {
    composable<HomeRouteKey> {

        HomeRoute(
            onNavigateToPlayer = onNavigateToPlayer,
            onNavigateToSearch = onNavigateToSearch,
            onOpenPlaylist = onOpenPlaylist,
        )
    }
}