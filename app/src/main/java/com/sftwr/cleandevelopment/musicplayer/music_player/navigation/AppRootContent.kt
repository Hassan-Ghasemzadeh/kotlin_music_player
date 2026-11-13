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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sftwr.cleandevelopment.musicplayer.data.util.LocaleHelper
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.BottomPlayerBar
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalAlbumArtworkEnabled
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalGlassmorphicEnabled
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SettingsDialog
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SlantedLeftEdgeShape
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SlantedRightEdgeShape
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SlantedNavigationDrawerContent
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.library.presentation.about.AboutDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer.EqualizerBottomSheet
import com.sftwr.cleandevelopment.musicplayer.library.presentation.rate.RateUsBottomSheet
import com.sftwr.cleandevelopment.musicplayer.library.presentation.scan.ScanMediaDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer.SleepTimerDialog
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import java.util.Locale

/**
 * Root application content composable responsible for hosting the navigation graph,
 * managing bidirectional layout direction (RTL/LTR), locale reconfiguration,
 * and dynamically displaying the persistent bottom mini-player bar and slanted drawer.
 */
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(InternalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppRootContent(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val playerManager = mainViewModel.playerManager
    val settings by mainViewModel.settingsState.collectAsStateWithLifecycle()

    val currentLanguage = settings.language
    val layoutDirection = when (currentLanguage) {
        AppLanguage.PERSIAN -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    val context = LocalContext.current
    LaunchedEffect(currentLanguage) {
        LocaleHelper.applyLanguage(context, currentLanguage)
        val locale = Locale(currentLanguage.code)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalGlassmorphicEnabled provides settings.glassmorphicStyleEnabled,
        LocalAlbumArtworkEnabled provides settings.isAlbumArtworkEnabled
    ) {
        val currentSong by playerManager.currentSong.collectAsStateWithLifecycle()
        val isPlaying by playerManager.isPlaying.collectAsStateWithLifecycle()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute?.contains("Player") == false &&
                currentRoute?.contains("Settings") == false &&
                currentRoute?.contains("Visualizer") == false

        val artUri by produceState<Bitmap?>(initialValue = null, key1 = currentSong?.uri) {
            val songUri = currentSong?.uri
            value = if (songUri != null) {
                withContext(Dispatchers.IO) {
                    songUri.toUri().extractAlbumArt(context)
                }
            } else null
        }

        var isDrawerOpen by remember { mutableStateOf(false) }
        var selectedDrawerItem by remember { mutableStateOf("Equalizer") }

        val drawerProgress by animateFloatAsState(
            targetValue = if (isDrawerOpen) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )

        var showEqualizerDialog by remember { mutableStateOf(false) }
        var showSleepTimerDialog by remember { mutableStateOf(false) }
        var showScanMediaDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }
        var showRateUsBottomSheet by remember { mutableStateOf(false) }

        val density = LocalDensity.current
        val maxTranslationX = with(density) { 240.dp.toPx() }
        val maxTranslationY = with(density) { 24.dp.toPx() }
        val isRtl = layoutDirection == LayoutDirection.Rtl

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2C1454))
        ) {
            // 1. Drawer Content rendered underneath
            SlantedNavigationDrawerContent(
                selectedItem = selectedDrawerItem,
                onItemSelected = { selectedDrawerItem = it },
                onEqualizerClick = { showEqualizerDialog = true },
                onSleepTimerClick = { showSleepTimerDialog = true },
                onScanMediaClick = { showScanMediaDialog = true },
                onPermissionsClick = { navController.navigate(Screen.Permissions) },
                onSettingsClick = { navController.navigate(Screen.Settings) },
                onAboutClick = { showAboutDialog = true },
                onRateUsClick = { showRateUsBottomSheet = true },
                onCloseDrawer = { isDrawerOpen = false }
            )

            // 2. Main Content Card on top
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = if (isRtl) -drawerProgress * maxTranslationX else drawerProgress * maxTranslationX
                        translationY = drawerProgress * maxTranslationY
                        scaleX = 1f - (drawerProgress * 0.12f)
                        scaleY = 1f - (drawerProgress * 0.20f)
                        rotationZ = if (isRtl) drawerProgress * 5f else drawerProgress * -5f
                        transformOrigin = if (isRtl) TransformOrigin(1f, 0.15f) else TransformOrigin(0f, 0.15f)
                        cameraDistance = 8f * density.density
                    }
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp * drawerProgress.coerceAtLeast(0f),
                            topEnd = 24.dp * drawerProgress.coerceAtLeast(0f)
                        )
                    )
                    .background(Color(0xFF0F0B21))
                    .clickable(
                        enabled = isDrawerOpen,
                        onClick = { isDrawerOpen = false }
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
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

        if (showEqualizerDialog) {
            EqualizerBottomSheet(onDismissRequest = { showEqualizerDialog = false })
        }
        if (showSleepTimerDialog) {
            SleepTimerDialog(onDismiss = { showSleepTimerDialog = false })
        }
        if (showScanMediaDialog) {
            ScanMediaDialog(onDismiss = { showScanMediaDialog = false })
        }
        if (showSettingsDialog) {
            SettingsDialog(onDismiss = { showSettingsDialog = false })
        }
        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }
        if (showRateUsBottomSheet) {
            RateUsBottomSheet(onDismiss = { showRateUsBottomSheet = false })
        }
    }
}
