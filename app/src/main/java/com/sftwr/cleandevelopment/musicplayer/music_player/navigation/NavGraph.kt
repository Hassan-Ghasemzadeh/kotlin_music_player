package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.HomeRouteKey
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.PlayerRouteKey
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.homeGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.PlayerScreen
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TracksRouteKey
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.tracksGraph

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRouteKey,
        modifier = modifier
    ) {
        // home main graph
        homeGraph(
            onNavigateToPlayer = { songId ->
                navController.navigate(PlayerRouteKey(songId))
            },
            onOpenPlaylist = {

            },
            onNavigateToSearch = {

            },
            onNavigateToTracks = {
                // Navigate to Tracks screen when card is clicked
                navController.navigate(TracksRouteKey)
            }
        )
        // Tracks graph
        tracksGraph(
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {

            },
            onNavigateToSearch = {},
            onNavigateToPlayer = { songId ->
                navController.navigate(PlayerRouteKey(songId))
            }
        )
        composable<PlayerRouteKey> { backStackEntry ->
            val playerRoute: PlayerRouteKey = backStackEntry.toRoute()
            val songId = playerRoute.songId

            PlayerScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

    }
}