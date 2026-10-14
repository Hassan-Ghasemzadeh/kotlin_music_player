package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sftwr.cleandevelopment.musicplayer.home.HomeRouteKey
import com.sftwr.cleandevelopment.musicplayer.home.PlayerRouteKey
import com.sftwr.cleandevelopment.musicplayer.home.homeGraph

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

            }
        )
        composable<PlayerRouteKey> { backStackEntry ->
            val playerRoute: PlayerRouteKey = backStackEntry.toRoute()
            val songId = playerRoute.songId

            // PlayerScreen(songId = songId)
        }

    }
}