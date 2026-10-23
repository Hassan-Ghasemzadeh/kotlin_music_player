package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

// Define type-safe route keys for home feature
@Serializable
data object TracksRouteKey

/**
 * Extension function to register the tracks screen and its navigation graph
 * without exposing internal ViewModels or UI components to the :app module.
 */
fun NavGraphBuilder.tracksGraph(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onBackClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit,
) {
    composable<TracksRouteKey> {

        TracksRoute(
            onBackClick = onBackClick,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToPlayer = onNavigateToPlayer,
            onInfoClick = onInfoClick,
            onTagEditClick = onTagEditClick,
        )
    }
}