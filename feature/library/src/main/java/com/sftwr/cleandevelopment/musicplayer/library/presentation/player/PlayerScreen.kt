package com.sftwr.cleandevelopment.musicplayer.library.presentation.player

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.core.util.toMillis
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.AudioVisualizerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.CircularVisualizerType
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel.PlayerViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel.RepeatMode
import com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer.NeonCircularVisualizer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    // Collecting player state flows from ViewModel
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val songState by viewModel.currentSong.collectAsState()

    val totalDuration = songState?.duration ?: ""
    val songTitle = songState?.title ?: "Unknown Title"
    val artistName = songState?.artist ?: "Unknown Artist"

    val context = LocalContext.current
    val visualizerManager = remember { AudioVisualizerManager() }

    // 1. Check RECORD_AUDIO permission for the audio visualizer
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
        songState?.let { song ->
            Log.d("AlbumArtDebug", "Song Title: ${song.title}")
            Log.d("AlbumArtDebug", "Song URI / Path: ${song.uri}")
            // اگر در مدل شما فیلد دیگری برای آلبوم آرت هست:
            // Log.d("AlbumArtDebug", "Album Art Uri: ${song.albumArtUri}")
        }
    }

    // 2. Bind the visualizer to the active player session
    LaunchedEffect(isPlaying, hasAudioPermission) {
        if (isPlaying && hasAudioPermission) {
            delay(350.milliseconds)
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

    // Ensure visualizer resources are released when leaving the screen
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Header / Top Bar
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
                text = "Now Playing", style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f)
                ), textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Neon Visualizer and Album Art Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            NeonCircularVisualizer(
                type = CircularVisualizerType.NEON_FREQUENCY_BARS,
                fftData = fftData,
                albumArtUrl = songState?.uri,
                isPlaying = isPlaying,
                size = 280.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Track Information (Title & Artist)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = songTitle, style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center
                ), maxLines = 1, modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = artistName, style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                ), maxLines = 1, modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Playback Controls & Timeline Slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPosition.formatTime(),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f))
                )
                Text(
                    text = totalDuration,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f))
                )
            }

            // Custom modern slim slider
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

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Actions Row (Shuffle, Previous, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) Color(0xFF00D2FF) else Color.White.copy(alpha = 0.6f)
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
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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