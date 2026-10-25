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
            onNavigateToPlayer = {
                navController.navigate(Screen.Player)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = {
                navController.navigate(Screen.EditTagScreen(it))
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
                },
                onTagEditClick = {
                    navController.navigate(Screen.EditTagScreen(it))
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
        editTagScreenGraph(
            onBackClick = {
                navController.popBackStack()
            },
        )
    }
}