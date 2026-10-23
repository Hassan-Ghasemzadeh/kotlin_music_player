package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.BottomPlayerBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import kotlinx.serialization.InternalSerializationApi

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(InternalSerializationApi::class)
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
    val showBottomBar = currentRoute?.contains("Player") == false
    val context = LocalContext.current

    val artUri = remember(currentSong?.uri) {
        currentSong?.uri?.toUri()?.extractAlbumArt(context = context)
    }
    Box(
        modifier = Modifier
            .background(Color(0xFF0F0B21))
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AppNavGraph(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            navController = navController,
        )
        if (showBottomBar && currentSong != null) {
            // Only show if a song is selected/playing
            BottomPlayerBar(
                hasNext = playerManager.hasNext,
                hasPrevious = playerManager.hasPrevious,
                songTitle = currentSong!!.title,
                artistName = currentSong!!.artist,
                artUri = artUri,
                isPlaying = isPlaying,
                onPlayPauseClick = { playerManager.togglePlayPause() },
                onPlayerClick = {
                    // Navigate to full player screen if needed
                    navController.navigate(Screen.Player)
                },
                onNextClick = {
                    playerManager.playNext()
                },
                onPreviousClick = {
                    playerManager.playPrevious()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 5.dp)
            )
        }
    }
}