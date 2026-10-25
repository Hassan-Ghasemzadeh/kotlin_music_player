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

/**
 * Root application content composable responsible for hosting the navigation graph
 * and dynamically displaying the persistent bottom mini-player bar based on the active screen state.
 */
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(InternalSerializationApi::class)
@Composable
fun AppRootContent(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    // Retrieve the centralized audio player manager instance from the main view model
    val playerManager = mainViewModel.playerManager

    // Collect active playback state flows reactively using lifecycle-aware extension
    val currentSong by playerManager.currentSong.collectAsStateWithLifecycle()
    val isPlaying by playerManager.isPlaying.collectAsStateWithLifecycle()

    // Monitor current navigation backstack entry to determine active route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom player bar when the user is currently on the full Player screen
    val showBottomBar = currentRoute?.contains("Player") == false
    val context = LocalContext.current

    // Extract and remember album art bitmap efficiently when the current song URI changes
    val artUri = remember(currentSong?.uri) {
        currentSong?.uri?.toUri()?.extractAlbumArt(context = context)
    }

    // Root container box holding the navigation graph and layered bottom player bar
    Box(
        modifier = Modifier
            .background(Color(0xFF0F0B21))
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main navigation graph container for screens
        AppNavGraph(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            navController = navController,
        )

        // Conditionally render the bottom mini player bar if a song is loaded and visibility rules allow
        if (showBottomBar && currentSong != null) {
            BottomPlayerBar(
                hasNext = playerManager.hasNext,
                hasPrevious = playerManager.hasPrevious,
                songTitle = currentSong!!.title,
                artistName = currentSong!!.artist,
                artUri = artUri,
                isPlaying = isPlaying,
                onPlayPauseClick = { playerManager.togglePlayPause() },
                onPlayerClick = {
                    // Navigate to full-screen player view when mini player is tapped
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