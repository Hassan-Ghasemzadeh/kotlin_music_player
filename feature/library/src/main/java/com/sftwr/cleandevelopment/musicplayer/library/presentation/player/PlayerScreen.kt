package com.sftwr.cleandevelopment.musicplayer.library.presentation.player

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.sftwr.cleandevelopment.musicplayer.home.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import androidx.compose.ui.graphics.Brush
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
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.core.util.toMillis
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.AudioVisualizerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.CircularVisualizerType
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.AddToPlaylistDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer.EqualizerBottomSheet
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel.PlayerViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel.RepeatMode
import com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer.SleepTimerDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer.NeonCircularVisualizer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onBackClick: () -> Unit,
) {
    // Collecting player state flows reactively from the ViewModel with lifecycle awareness
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val songState by viewModel.currentSong.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val isAddToPlaylistDialogVisible by viewModel.isAddToPlaylistDialogVisible.collectAsStateWithLifecycle()
    val visualizerStyle by viewModel.visualizerStyle.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val playbackPitch by viewModel.playbackPitch.collectAsStateWithLifecycle()

    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showAudioControlsSheet by remember { mutableStateOf(false) }

    val totalDuration = songState?.duration ?: ""
    val songTitle = songState?.title ?: stringResource(R.string.unknown_title)
    val artistName = songState?.artist ?: stringResource(R.string.unknown_artist)

    val context = LocalContext.current
    val activity = context as? android.app.Activity
    DisposableEffect(Unit) {
        val window = activity?.window
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        controller?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            controller?.let {
                it.show(WindowInsetsCompat.Type.systemBars())
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

    // Bind and control the visualizer lifecycle based on playback state, active song, and audio recording permissions
    LaunchedEffect(isPlaying, hasAudioPermission, songState?.id) {
        if (isPlaying && hasAudioPermission) {
            delay(200.milliseconds)
            val sessionId = viewModel.audioSessionId
            Log.d("VisualizerDebug", "Active Session ID: $sessionId")

            if (sessionId > 0) {
                visualizerManager.startListening(sessionId)
            }
        } else {
            visualizerManager.stopListening()
        }
    }

    val fftData by visualizerManager.fftData.collectAsState()
    val swipeArtworkToSkip by viewModel.swipeArtworkToSkip.collectAsState()
    val density = LocalDensity.current
    val screenWidthPx = with(density) { 350.dp.toPx() }
    var dragOffset by remember { mutableStateOf(0f) }
    val animatedOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()

    val artworkModifier = if (swipeArtworkToSkip) {
        Modifier
            .graphicsLayer { translationX = animatedOffset.value }
            .pointerInput(swipeArtworkToSkip) {
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

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0B21), Color(0xFF181035), Color(0xFF2C1454)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
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
                    .padding(vertical = 8.dp),
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
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Playing Queue",
                            tint = Color(0xFF91D8FB),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showEqualizerSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = Color(0xFF91D8FB),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Neon circular visualizer and album art container view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(artworkModifier),
                contentAlignment = Alignment.Center
            ) {
                NeonCircularVisualizer(
                    type = visualizerStyle.type,
                    fftData = fftData,
                    albumArtUrl = songState?.uri,
                    isPlaying = isPlaying,
                    size = 310.dp,
                    discRatio = 0.80f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Favorite, Add to Playlist, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFavorite = songState?.isFavorite == true
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = Color(0xFF91D8FB),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { viewModel.openAddToPlaylistDialog() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = Color(0xFF91D8FB),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { showSleepTimerDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = Color(0xFF91D8FB),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { showAudioControlsSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Audio Controls",
                        tint = Color(0xFF91D8FB),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { viewModel.shareSong(context) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF91D8FB),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active track information (Title & Artist name labels)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
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
                Column(modifier = Modifier.fillMaxWidth()) {
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
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = {
                            viewModel.seekTo(it.toLong())
                        },
                        valueRange = 0f..(if (totalDuration.toMillis() > 0f) totalDuration.toMillis() else 1f),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFF6200EE), CircleShape)
                            )
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFF6200EE),
                                    inactiveTrackColor = Color(0x33A855F7)
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
                                tint = if (isShuffleEnabled) Color(0xFF00D2FF) else Color.White.copy(
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE)),
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
                                    else -> Color(0xFF00D2FF)
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
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F0B21),
                                        Color(0xFF181035),
                                        Color(0xFF2C1454)
                                    )
                                )
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
                                            containerColor = if (isCurrentSong) Color(0xFF6200EE).copy(
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
                                                        Color(0x33FFFFFF),
                                                        MaterialTheme.shapes.small
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isCurrentSong && isPlaying) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Playing",
                                                        tint = Color(0xFF91D8FB),
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
                                                        color = if (isCurrentSong) Color(0xFF91D8FB) else Color.White
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
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F0B21), Color(0xFF181035), Color(0xFF2C1454))
                                )
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
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                                    )
                                    Text(
                                        text = "${(volume * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF91D8FB), fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = volume,
                                    onValueChange = { viewModel.setVolume(it) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF91D8FB),
                                        activeTrackColor = Color(0xFF91D8FB),
                                        inactiveTrackColor = Color(0x3391D8FB)
                                    )
                                )
                            }

                            // Pitch / Speed Slider
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.playback_pitch),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                                    )
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.2fx", playbackPitch),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF91D8FB), fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = playbackPitch,
                                    onValueChange = { viewModel.setPlaybackPitch(it) },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF91D8FB),
                                        activeTrackColor = Color(0xFF91D8FB),
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
}
