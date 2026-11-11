package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.AboutDialog
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.BottomPlayerBar
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.NeonBlue
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SettingsDialog
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SlantedLeftEdgeShape
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SlantedNavigationDrawerContent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer.SleepTimerDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.scan.ScanMediaDialog
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer.EqualizerBottomSheet
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi

/**
 * Root application content composable responsible for hosting the navigation graph
 * and dynamically displaying the persistent bottom mini-player bar based on the active screen state.
 */
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(InternalSerializationApi::class, ExperimentalMaterial3Api::class)
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

    // Extract album art bitmap asynchronously on IO thread pool when current song changes
    val artUri by produceState<Bitmap?>(initialValue = null, key1 = currentSong?.uri) {
        val songUri = currentSong?.uri
        value = if (songUri != null) {
            withContext(Dispatchers.IO) {
                songUri.toUri().extractAlbumArt(context)
            }
        } else null
    }

    // Drawer open/closed state
    var isDrawerOpen by remember { mutableStateOf(false) }
    var selectedDrawerItem by remember { mutableStateOf("Equalizer") }
    val coroutineScope = rememberCoroutineScope()

    // Animation progress for sliding, scaling, rotating main content card
    val drawerProgress by animateFloatAsState(
        targetValue = if (isDrawerOpen) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    // Dialog states for menu actions
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showScanMediaDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val maxTranslationX = with(density) { 240.dp.toPx() }
    val maxTranslationY = with(density) { 24.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C1454)) // Drawer purple background base matching reference design
    ) {
        // 1. Drawer Content rendered underneath (Eagerly composed so first click is instantaneous)
        SlantedNavigationDrawerContent(
            selectedItem = selectedDrawerItem,
            onItemSelected = { selectedDrawerItem = it },
            onEqualizerClick = { showEqualizerDialog = true },
            onSleepTimerClick = { showSleepTimerDialog = true },
            onScanMediaClick = { showScanMediaDialog = true },
            onPermissionsClick = { navController.navigate(Screen.Permissions) },
            onSettingsClick = { showSettingsDialog = true },
            onAboutClick = { showAboutDialog = true },
            onRateUsClick = { selectedDrawerItem = "Rate Us" },
            onCloseDrawer = { isDrawerOpen = false }
        )

        // 2. Main Content Card on top (shifts right, slightly reduced height when open, rotates left, and applies slanted edge)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = drawerProgress * maxTranslationX
                    translationY = drawerProgress * maxTranslationY
                    scaleX = 1f - (drawerProgress * 0.12f)
                    scaleY = 1f - (drawerProgress * 0.20f)
                    rotationZ = drawerProgress * -5f
                    transformOrigin = TransformOrigin(0f, 0.15f)
                    cameraDistance = 8f * density.density
                }
                .clip(
                    if (drawerProgress > 0.01f) SlantedLeftEdgeShape
                    else androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                )
                .background(Color(0xFF0F0B21))
                .clickable(
                    enabled = isDrawerOpen,
                    onClick = { isDrawerOpen = false }
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Main navigation graph container for screens
            AppNavGraph(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                navController = navController,
                playerManager = playerManager,
                onOpenDrawer = {
                    isDrawerOpen = !isDrawerOpen
                }
            )

            // Conditionally render the bottom mini player bar if a song is loaded and visibility rules allow
            if (showBottomBar && currentSong != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    BottomPlayerBar(
                        hasNext = playerManager.hasNext,
                        hasPrevious = playerManager.hasPrevious,
                        songTitle = currentSong!!.title,
                        artistName = currentSong!!.artist,
                        artUri = artUri,
                        isPlaying = isPlaying,
                        onPlayPauseClick = { playerManager.togglePlayPause() },
                        onPlayerClick = {
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
    }

    // Equalizer Bottom Sheet
    if (showEqualizerDialog) {
        EqualizerBottomSheet(
            onDismissRequest = { showEqualizerDialog = false }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    // Scan Media Dialog
    if (showScanMediaDialog) {
        ScanMediaDialog(
            onDismiss = { showScanMediaDialog = false }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    // About Us Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}
