package com.sftwr.cleandevelopment.musicplayer.library.presentation.player

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.sftwr.cleandevelopment.musicplayer.home.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalColorPalette
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime

import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.AddToPlaylistDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer.EqualizerBottomSheet
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel.PlayerViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel.RepeatMode
import com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer.SleepTimerDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer.NeonCircularVisualizer
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import androidx.compose.material.icons.filled.Search
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.AudioVisualizerManager
import com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics.LyricSourceDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics.ManualLyricEditDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics.NoLyricPromptView
import com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics.OnlineLyricSearchDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics.SynchronizedLyricView
import com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics.viewmodel.LyricViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

/**
 * PlayerScreen displays the full-screen media player interface featuring dynamic neon circular visualizers,
 * album artwork, playback timeline scrubbers, action buttons (Favorite, Add to Playlist, Share),
 * and core audio transport control buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    lyricViewModel: LyricViewModel = hiltViewModel(),
    sheetProgress: Float = 1f,
    onBackClick: () -> Unit,
    onNavigateToVisualizerSelection: () -> Unit = {},
    onNavigateToSoundEffect: () -> Unit = {},
    onNavigateToLyricManagement: () -> Unit = {}
) {
    // Collecting player state flows reactively from the ViewModel with lifecycle awareness
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val songState by viewModel.currentSong.collectAsStateWithLifecycle()
    val currentLyric by viewModel.currentLyric.collectAsStateWithLifecycle()
    val onlineSearchState by lyricViewModel.onlineSearchState.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val isAddToPlaylistDialogVisible by viewModel.isAddToPlaylistDialogVisible.collectAsStateWithLifecycle()
    val visualizerStyle by viewModel.visualizerStyle.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val playbackPitch by viewModel.playbackPitch.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()

    val accent = LocalColorPalette.current.accent
    val gradientBrush = LocalColorPalette.current.gradientBrush

    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showAudioControlsSheet by remember { mutableStateOf(false) }

    var isLyricOverlayActive by remember { mutableStateOf(false) }
    var showLyricSourceDialog by remember { mutableStateOf(false) }
    var showOnlineSearchDialog by remember { mutableStateOf(false) }
    var showManualEditDialog by remember { mutableStateOf(false) }

    val totalDuration = songState?.duration ?: ""
    val songTitle = songState?.title ?: stringResource(R.string.unknown_title)
    val artistName = songState?.artist ?: stringResource(R.string.unknown_artist)

    val context = LocalContext.current

    val lrcFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val songId = songState?.id?.toLongOrNull() ?: -1L
            if (songId > 0) {
                lyricViewModel.loadAndSaveLrcFromUri(context, songId, songTitle, artistName, uri)
                isLyricOverlayActive = true
            }
        }
    }

    val visualizerManager = remember { AudioVisualizerManager() }

    LaunchedEffect(key1 = true) {
        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Check RECORD_AUDIO permission required for capturing active audio session frequency/FFT data
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted && isPlaying) {
            val sessionId = viewModel.audioSessionId
            if (sessionId > 0) {
                visualizerManager.startListening(sessionId)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val sessionId = viewModel.audioSessionId

    // Bind and control the visualizer lifecycle based on playback state, active song, and audio recording permissions
    DisposableEffect(isPlaying, hasAudioPermission, songState?.id, sessionId) {
        if (isPlaying && hasAudioPermission && sessionId > 0) {
            Log.d("VisualizerDebug", "Active Session ID: $sessionId")
            visualizerManager.startListening(sessionId)
        } else {
            visualizerManager.stopListening()
        }
        onDispose {
            visualizerManager.stopListening()
        }
    }

    val fftData by visualizerManager.fftData.collectAsState()
    val swipeArtworkToSkip by viewModel.swipeArtworkToSkip.collectAsState()
    val density = LocalDensity.current
    val screenWidthPx = with(density) { 350.dp.toPx() }
    var dragOffset by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val animatedOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()

    val artworkModifier = if (swipeArtworkToSkip) {
        Modifier
            .graphicsLayer { translationX = animatedOffset.value }
            .pointerInput(true) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (dragOffset < -80f) {
                                animatedOffset.animateTo(
                                    -screenWidthPx,
                                    animationSpec = androidx.compose.animation.core.tween(200)
                                )
                                viewModel.skipNext()
                                animatedOffset.snapTo(screenWidthPx)
                                animatedOffset.animateTo(
                                    0f,
                                    animationSpec = androidx.compose.animation.core.tween(200)
                                )
                            } else if (dragOffset > 80f) {
                                animatedOffset.animateTo(
                                    screenWidthPx,
                                    animationSpec = androidx.compose.animation.core.tween(200)
                                )
                                viewModel.skipPrevious()
                                animatedOffset.snapTo(-screenWidthPx)
                                animatedOffset.animateTo(
                                    0f,
                                    animationSpec = androidx.compose.animation.core.tween(200)
                                )
                            } else {
                                animatedOffset.animateTo(
                                    0f,
                                    animationSpec = androidx.compose.animation.core.spring()
                                )
                            }
                            dragOffset = 0f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        scope.launch {
                            animatedOffset.snapTo(animatedOffset.value + dragAmount)
                        }
                    }
                )
            }
    } else {
        Modifier
    }

    // Ensure audio visualizer resources are cleanly released when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            visualizerManager.stopListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top screen header bar with collapse button and equalizer button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .graphicsLayer {
                        alpha = ((sheetProgress - 0.2f) / 0.8f).coerceIn(0f, 1f)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = stringResource(com.sftwr.cleandevelopment.musicplayer.home.R.string.now_playing),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f)
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Strictly conditional magnifying glass icon for lyrics management/viewing
                    if (currentLyric != null) {
                        IconButton(onClick = { showLyricSourceDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.lyric_title),
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Playing Queue",
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showEqualizerSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Neon circular visualizer, album art, or interactive lyrics container view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        val scale = 0.6f + 0.4f * sheetProgress
                        scaleX = scale
                        scaleY = scale
                        alpha = ((sheetProgress - 0.05f) / 0.95f).coerceIn(0f, 1f)
                    }
                    .then(artworkModifier)
                    .clickable {
                        isLyricOverlayActive = !isLyricOverlayActive
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLyricOverlayActive) {
                    val activeLyric = currentLyric
                    if (activeLyric != null) {
                        SynchronizedLyricView(
                            lyric = activeLyric,
                            currentPositionMs = currentPosition,
                            onSeekTo = { seekMs -> viewModel.seekTo(seekMs) },
                            onEditClick = { showManualEditDialog = true },
                            onCloseClick = { isLyricOverlayActive = false }
                        )
                    } else {
                        NoLyricPromptView(
                            onClick = { showLyricSourceDialog = true }
                        )
                    }
                } else {
                    NeonCircularVisualizer(
                        type = visualizerStyle.type,
                        fftData = fftData,
                        albumArtUrl = songState?.uri,
                        isPlaying = isPlaying,
                        size = 390.dp,
                        discRatio = 0.82f
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Favorite, Add to Playlist, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .graphicsLayer {
                        alpha = ((sheetProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFavorite = songState?.isFavorite == true
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { viewModel.openAddToPlaylistDialog() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onNavigateToSoundEffect) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = stringResource(com.sftwr.cleandevelopment.musicplayer.home.R.string.sound_effects),
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { showSleepTimerDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onNavigateToVisualizerSelection) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Visualizer Style",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { showAudioControlsSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Audio Controls",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { viewModel.shareSong(context) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active track information (Title & Artist name labels)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = ((sheetProgress - 0.15f) / 0.85f).coerceIn(0f, 1f)
                    }
            ) {
                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback progress timeline slider and duration indicator labels
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = ((sheetProgress - 0.2f) / 0.8f).coerceIn(0f, 1f)
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentPosition.formatTime(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(
                                    alpha = 0.6f
                                )
                            )
                        )
                        Text(
                            text = totalDuration,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(
                                    alpha = 0.6f
                                )
                            )
                        )
                    }

                    // Custom modern slim playback progress slider
                    val durationMs = songState?.durationInMs?.toFloat() ?: 0f
                    val maxDuration = if (durationMs > 0f) durationMs else 1f
                    Slider(
                        value = currentPosition.toFloat().coerceIn(0f, maxDuration),
                        onValueChange = {
                            viewModel.seekTo(it.toLong())
                        },
                        valueRange = 0f..maxDuration,
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        accent,
                                        CircleShape
                                    )
                            )
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                colors = SliderDefaults.colors(
                                    activeTrackColor = accent,                    // مسیر طی شده با رنگ اکسنتر نئون
                                    inactiveTrackColor = accent.copy(alpha = 0.2f) // مسیر باقی‌مانده با کمترین شفافیت از همان رنگ
                                ),
                                enabled = true,
                                sliderState = sliderState,
                                modifier = Modifier.height(4.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Playback controls action bar (Shuffle, Previous, Play/Pause, Next, Repeat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleShuffle() }) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleEnabled) accent else Color.White.copy(
                                    alpha = 0.6f
                                )
                            )
                        }
                        IconButton(onClick = { viewModel.skipPrevious() }) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = accent),
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier
                                .size(72.dp)
                                .clickable {
                                    viewModel.togglePlayPause()
                                }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.skipNext() }) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.toggleRepeat() }) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    RepeatMode.ONE -> Icons.Default.RepeatOne
                                    else -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = when (repeatMode) {
                                    RepeatMode.OFF -> Color.White.copy(alpha = 0.6f)
                                    else -> accent
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add to Playlist Dialog prompt
        if (isAddToPlaylistDialogVisible && songState != null) {
            AddToPlaylistDialog(
                songTitle = songState!!.title,
                playlists = playlists,
                isFavorite = songState!!.isFavorite,
                onSelectPlaylist = { playlist ->
                    viewModel.addSongToPlaylist(playlist)
                },
                onToggleFavorite = {
                    viewModel.toggleFavorite()
                },
                onDismissRequest = {
                    viewModel.closeAddToPlaylistDialog()
                }
            )
        }

        // Equalizer Custom Bottom Sheet
        if (showEqualizerSheet) {
            EqualizerBottomSheet(
                onDismissRequest = {
                    showEqualizerSheet = false
                }
            )
        }

        // Sleep Timer Dialog
        if (showSleepTimerDialog) {
            SleepTimerDialog(
                onDismiss = {
                    showSleepTimerDialog = false
                }
            )
        }

        // Playing Queue Modal Bottom Sheet
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color.Transparent,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.4f)) }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)
                            .background(
                                brush = gradientBrush
                            )
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.playing_queue),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "${queue.size} tracks",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(queue) { index, song ->
                                    val isCurrentSong = song.id == songState?.id
                                    Card(
                                        onClick = {
                                            viewModel.playQueueItem(index)
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrentSong) accent.copy(
                                                alpha = 0.3f
                                            ) else Color(0x11FFFFFF)
                                        ),
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        color = if (isCurrentSong) accent.copy(alpha = 0.3f) else Color(
                                                            0x22FFFFFF
                                                        ),
                                                        shape = MaterialTheme.shapes.small
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCurrentSong && isPlaying) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Playing",
                                                        tint = accent,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "${index + 1}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = Color.White.copy(alpha = 0.7f),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = song.title,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isCurrentSong) accent else Color.White
                                                    ),
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = song.artist,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.White.copy(alpha = 0.6f)
                                                    ),
                                                    maxLines = 1
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.removeFromQueue(index) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove from Queue",
                                                    tint = Color.White.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Audio Controls Modal Bottom Sheet
        if (showAudioControlsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAudioControlsSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color.Transparent,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.4f)) }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = gradientBrush
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.audio_controls),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            // Volume Slider
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.playback_volume),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    )
                                    Text(
                                        text = "${(volume * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = volume,
                                    onValueChange = { viewModel.setVolume(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accent,
                                        activeTrackColor = accent,
                                        inactiveTrackColor = Color(0x3391D8FB)
                                    )
                                )
                            }

                            // Pitch Slider
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.playback_pitch),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    )
                                    Text(
                                        text = String.format(
                                            java.util.Locale.US,
                                            "%.2fx",
                                            playbackPitch
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = playbackPitch,
                                    onValueChange = { viewModel.setPlaybackPitch(it) },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accent,
                                        activeTrackColor = accent,
                                        inactiveTrackColor = Color(0x3391D8FB)
                                    )
                                )
                            }

                            // Playback Speed Slider
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.playback_speed),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    )
                                    Text(
                                        text = String.format(
                                            java.util.Locale.US,
                                            "%.2fx",
                                            playbackSpeed
                                        ),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = playbackSpeed,
                                    onValueChange = { viewModel.setPlaybackSpeed(it) },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accent,
                                        activeTrackColor = accent,
                                        inactiveTrackColor = Color(0x3391D8FB)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showLyricSourceDialog) {
        LyricSourceDialog(
            onDismissRequest = { showLyricSourceDialog = false },
            onSelectOnlineSearch = {
                lyricViewModel.resetOnlineSearchState()
                showOnlineSearchDialog = true
            },
            onSelectLocalFile = {
                lrcFilePickerLauncher.launch("*/*")
            },
            onSelectManualEdit = {
                showManualEditDialog = true
            },
            onSelectManageSaved = {
                onNavigateToLyricManagement()
            }
        )
    }

    if (showOnlineSearchDialog) {
        OnlineLyricSearchDialog(
            initialTitle = songTitle,
            initialArtist = artistName,
            searchState = onlineSearchState,
            onSearch = { title, artist ->
                lyricViewModel.searchOnline(title, artist)
            },
            onSave = { rawContent ->
                val songId = songState?.id?.toLongOrNull() ?: -1L
                if (songId > 0) {
                    lyricViewModel.saveLyric(songId, songTitle, artistName, rawContent)
                    isLyricOverlayActive = true
                }
            },
            onDismissRequest = { showOnlineSearchDialog = false }
        )
    }

    if (showManualEditDialog) {
        ManualLyricEditDialog(
            initialContent = currentLyric?.content ?: "",
            onSave = { updatedContent ->
                val songId = songState?.id?.toLongOrNull() ?: -1L
                if (songId > 0) {
                    lyricViewModel.saveLyric(songId, songTitle, artistName, updatedContent)
                    isLyricOverlayActive = true
                }
            },
            onDismissRequest = { showManualEditDialog = false }
        )
    }
}
