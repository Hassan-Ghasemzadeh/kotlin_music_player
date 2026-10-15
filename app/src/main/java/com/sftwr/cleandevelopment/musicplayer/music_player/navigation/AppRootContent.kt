package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.BottomPlayerBar
import androidx.compose.ui.graphics.Color

@Composable
fun AppRootContent(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != "SearchRouteKey" && currentRoute != "PlayerRouteKey"

    Scaffold(
        containerColor = Color(0xFF122144),
        bottomBar = {
            if (showBottomBar) {
                BottomPlayerBar(
//                    onPlayerClick = {
//                        navController.navigate(Screen.Player(/* song data */))
//                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}