package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.homeGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.PlayerScreen
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.SearchRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.SongDetailRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TracksRouteKey
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.tracksGraph
import com.sftwr.cleandevelopment.musicplayer.model.Screen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = modifier
    ) {
        // home main graph
        homeGraph(
            onNavigateToPlayer = {
                navController.navigate(Screen.Player)
            },
            onOpenPlaylist = {

            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search)
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
            onNavigateToSearch = {
                navController.navigate(Screen.Search)
            },
            onNavigateToPlayer = { songId ->
                navController.navigate(Screen.Player)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            }
        )
        composable<Screen.Search> {
            SearchRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player)
                },
                onInfoClick = { songId ->
                    navController.navigate(Screen.SongDetail(songId))
                }
            )
        }
        composable<Screen.Player> {

            PlayerScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable<Screen.SongDetail> {

            SongDetailRoute(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

    }
}