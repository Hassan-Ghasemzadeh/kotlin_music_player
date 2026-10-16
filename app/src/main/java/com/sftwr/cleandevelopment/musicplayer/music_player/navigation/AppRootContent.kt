package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.BottomPlayerBar
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.PlayerRouteKey

@Composable
fun AppRootContent(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val playerManager = mainViewModel.playerManager
    // Collect current playing song from global manager
    val currentSong by playerManager.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playerManager.isPlaying.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "SearchRouteKey" && currentRoute != "PlayerRouteKey"

    Box(
        modifier = Modifier
            .background(Color(0xFF122144))
            .systemBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AppNavGraph(
            navController = navController,
        )
        if (showBottomBar && currentSong != null) {
            // Only show if a song is selected/playing
            BottomPlayerBar(
                songTitle = currentSong!!.title,
                artistName = currentSong!!.artist,
                isPlaying = isPlaying,
                onPlayPauseClick = { playerManager.togglePlayPause() },
                onPlayerClick = {
                    // Navigate to full player screen if needed
                    navController.navigate(PlayerRouteKey(currentSong!!.id))
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}