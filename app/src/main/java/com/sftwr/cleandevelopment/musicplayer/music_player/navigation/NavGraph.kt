package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.presentation.edit.editTagScreenGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.homeGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.PlayerScreen
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.SearchRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.SongDetailRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TracksRouteKey
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.tracksGraph
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Main application navigation graph hosting all top-level destinations, feature graphs,
 * and type-safe screen routes using Jetpack Compose Navigation.
 */
@RequiresApi(Build.VERSION_CODES.S)
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
        // Home feature navigation graph
        homeGraph(
            onNavigateToPlayer = {
                navController.navigate(Screen.Player)
            },
            onOpenPlaylist = {
                // Placeholder for future playlist detail navigation
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search)
            },
            onNavigateToTracks = {
                // Navigate to tracks library screen when card is selected
                navController.navigate(TracksRouteKey)
            }
        )

        // Tracks library feature navigation graph
        tracksGraph(
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Search destination screen route
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
                },
                onTagEditClick = { songId ->
                    navController.navigate(Screen.EditTagScreen(songId))
                }
            )
        }

        // Full-screen audio player destination route
        composable<Screen.Player> {
            PlayerScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        // Detailed song metadata view destination route
        composable<Screen.SongDetail> {
            SongDetailRoute(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        // Edit tag feature navigation graph
        editTagScreenGraph(
            onBackClick = {
                navController.popBackStack()
            },
        )
    }
}