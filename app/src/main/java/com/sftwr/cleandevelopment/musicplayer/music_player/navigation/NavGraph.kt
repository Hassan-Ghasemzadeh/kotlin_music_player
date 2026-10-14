package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.home.PlayerRouteKey
import com.sftwr.cleandevelopment.musicplayer.home.homeGraph
import com.sftwr.cleandevelopment.musicplayer.home.presentation.HomeScreen

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
            onNavigateToPlayer = { songId ->
                navController.navigate(PlayerRouteKey(songId))
            },
            onOpenPlaylist = {

            },
            onNavigateToSearch = {

            }
        )

        // player graph
        composable<Screen.Home> { backStackEntry ->
//            val playerArgs = backStackEntry.toRoute<Screen.Home>()
            HomeScreen()
        }
    }
}