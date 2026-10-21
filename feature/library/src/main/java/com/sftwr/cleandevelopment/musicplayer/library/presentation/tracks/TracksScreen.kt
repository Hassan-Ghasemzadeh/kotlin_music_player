package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.SongDropdownMenu
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.TracksViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer.AnimatedVisualizer
import kotlin.toString

/**
 * UI Model representing a song item in the tracks list.
 */
data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val uri: String,
)

/**
 * TracksScreen displays the list of all available audio tracks with a glassmorphic design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
    modifier: Modifier = Modifier,
    songs: List<SongUiModel>,
    isLoading: Boolean = false,
    onSongClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    viewModel: TracksViewModel = hiltViewModel(),
) {
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlayingGlobal by viewModel.isPlayerPlaying.collectAsState()
    // Track which song's options menu is currently expanded
    var expandedSongId by remember { mutableStateOf<String?>(null) }

    // Main container with dark vertical gradient background matching the home screen
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B21), Color(0xFF181035), Color(0xFF2C1454)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top App Bar section for Tracks screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Tracks (${songs.size})",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PullToRefreshBox(
                isRefreshing = viewModel.uiState.collectAsState().value.isRefreshing,
                onRefresh = { viewModel.refreshTracks() },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        // 1. Loading state
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF00D2FF)
                                )
                            }
                        }

                        // 2. Empty list state
                        songs.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No songs found on the device!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pull down to refresh or add new audio files.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 3. Populated list state
                        else -> {
                            val bottomPadding = rememberPlayerAwareBottomPadding()
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = bottomPadding)
                            ) {
                                items(songs, key = { it.id }) { song ->
                                    val isMenuExpanded = expandedSongId == song.id
                                    val isThisSongPlaying = (currentSong?.id == song.id) && isPlayingGlobal
                                    TrackItem(
                                        song = song,
                                        onClick = {
                                            onSongClick(song.id)
                                            Log.d(
                                                "TracksScreen",
                                                "clicked songId: ${song.id}, currentPlayingId: ${currentSong?.id}, isPlaying: $isThisSongPlaying"
                                            )
                                        },
                                        isMenuExpanded = isMenuExpanded,
                                        onMenuClick = {
                                            expandedSongId = song.id
                                        },
                                        onDismissMenu = {
                                            expandedSongId = null
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteCurrentOrSelectedSong(song.id.toLong())
                                        },
                                        onInfoClick = {
                                            song.id.toLongOrNull()?.let { id -> onInfoClick(id) }
                                        },
                                        isPlaying = isThisSongPlaying
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

/**
 * Individual glass-morphic list item representing a single track.
 */
@Composable
fun TrackItem(
    song: SongUiModel,
    onClick: () -> Unit,
    isMenuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onInfoClick: () -> Unit,
    isPlaying: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.07f), // Semi-transparent glass effect
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)) // Subtle border stroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Extract embedded album art bytes for the current track
                val context = LocalContext.current
                val albumArtBytes = remember(song.uri) {
                    song.uri.toUri().extractAlbumArt(context)
                }

                if (albumArtBytes != null) {
                    AsyncImage(
                        model = albumArtBytes,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    // Fallback placeholder box and music note icon when no album art is available
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Track title and artist name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlaying) {
                    AnimatedVisualizer(
                        modifier = Modifier.size(24.dp, 14.dp),
                        barCount = 3,
                        barColor = Color(0xFF91D8FB)
                    )
                } else {
                    // Song duration text
                    Text(
                        text = song.duration,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Box to anchor the Dropdown Menu correctly to the more-vert button
                Box {
                    // More options menu button
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Dropdown Menu anchored to this item
                    SongDropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = onDismissMenu,
                        onAddToPlaylist = {
                            // TODO: Handle Add to Playlist
                        },
                        onFavoriteClick = {
                            // TODO: Handle Favorite
                        },
                        onInfoClick = onInfoClick,
                        onTagEditClick = {
                            // TODO: Handle Tag Edit
                        },
                        onDeleteClick = onDeleteClick,
                    )
                }
            }
        }
    }
}

/**
 * Preview function for inspecting the TracksScreen UI layout in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun TracksScreenPreview() {
    TracksScreen(
        songs = listOf(
            SongUiModel(
                id = "1",
                title = "Golaye Kaghazi",
                artist = "Masoud Bakhtiari",
                duration = "05:51",
                uri = ""
            )
        ),
        isLoading = false,
        onSongClick = {},
        onBackClick = {},
        onSearchClick = {},
        onInfoClick = { _ -> },
    )
}