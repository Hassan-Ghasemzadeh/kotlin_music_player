package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.AlphabetIndexerBar
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.SongDropdownMenu
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.TracksViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer.AnimatedVisualizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI Model representing a song item in the tracks list.
 */
data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val year: String,
    val genre: String,
    val duration: String,
    val uri: String,
)

/**
 * TracksScreen displays the list of all available audio tracks with a glassmorphic design, floating action button, and A-Z fast scroller.
 */
@RequiresApi(Build.VERSION_CODES.S)
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
    onTagEditClick: (Long) -> Unit,
    viewModel: TracksViewModel = hiltViewModel(),
) {
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlayingGlobal by viewModel.isPlayerPlaying.collectAsState()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(
            firstVisibleItemIndex = viewModel.scrollIndex,
            firstVisibleItemScrollOffset = viewModel.scrollOffset
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.scrollIndex = index
                viewModel.scrollOffset = offset
            }
    }

    val coroutineScope = rememberCoroutineScope()
    // Track which song's options menu is currently expanded
    var expandedSongId by remember { mutableStateOf<String?>(null) }

    val alphabet = remember { ('A'..'Z').map { it.toString() } }

    // Main container with dark vertical gradient background matching the home screen
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            val bottomPadding = rememberPlayerAwareBottomPadding()
            val adjustedBottomPadding = (bottomPadding - 60.dp).coerceAtLeast(8.dp)
            AnimatedVisibility(
                visible = currentSong != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                currentSong?.let { playingSong ->
                    FloatingActionButton(
                        onClick = {
                            val targetIndex = songs.indexOfFirst { it.id == playingSong.id }
                            if (targetIndex != -1) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        },
                        containerColor = Color(0xFF91D8FB),
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(bottom = adjustedBottomPadding)
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Go to Playing Song"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F0B21), Color(0xFF181035), Color(0xFF2C1454)
                        )
                    )
                )
                .padding(innerPadding)
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

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        // 1. Loading state (only show full screen loader if list is currently empty)
                        isLoading && songs.isEmpty() -> {
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

                        // 3. Populated list state with A-Z Fast Scroller
                        else -> {
                            val bottomPadding = rememberPlayerAwareBottomPadding()

                            Box(modifier = Modifier.fillMaxSize()) {
                                // LazyColumn با پدینگ سمت راست برای جلوگیری از تداخل با نوار الفبا
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(end = 32.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = bottomPadding)
                                ) {
                                    items(songs, key = { it.id }) { song ->
                                        val isMenuExpanded = expandedSongId == song.id
                                        val isThisSongPlaying =
                                            (currentSong?.id == song.id) && isPlayingGlobal
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
                                                song.id.toLongOrNull()
                                                    ?.let { id -> onInfoClick(id) }
                                            },
                                            onTagEditClick = {
                                                song.id.toLongOrNull()
                                                    ?.let { id ->
                                                        onTagEditClick(id)
                                                    }
                                            },
                                            isPlaying = isThisSongPlaying
                                        )
                                    }
                                }

                                AlphabetIndexerBar(
                                    letters = alphabet,
                                    onLetterSelected = { targetLetter ->
                                        val index = songs.indexOfFirst { song ->
                                            song.title.startsWith(
                                                targetLetter,
                                                ignoreCase = true
                                            )
                                        }
                                        if (index != -1) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(index)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(bottom = bottomPadding)
                                        .padding(end = 2.dp)
                                )
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
    onTagEditClick: () -> Unit,
    isPlaying: Boolean = false,
) {
    val borderColor = if (isPlaying) Color(0xFF91D8FB) else Color.White.copy(alpha = 0.12f)
    val backgroundColor =
        if (isPlaying) Color(0xFF91D8FB).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.07f)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(if (isPlaying) 1.5.dp else 1.dp, borderColor)
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
                val context = LocalContext.current
                val albumArtBitmap by produceState<Bitmap?>(initialValue = null, key1 = song.uri) {
                    value = withContext(Dispatchers.IO) {
                        song.uri.toUri().extractAlbumArt(context)
                    }
                }
                if (albumArtBitmap != null) {
                    AsyncImage(
                        model = albumArtBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                } else {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = if (isPlaying) Color(0xFF91D8FB) else Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
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
                    Text(
                        text = song.duration,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {
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

                    SongDropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = onDismissMenu,
                        onAddToPlaylist = {},
                        onFavoriteClick = {},
                        onInfoClick = onInfoClick,
                        onTagEditClick = onTagEditClick,
                        onDeleteClick = onDeleteClick,
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
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
                uri = "",
                album = "",
                year = "",
                genre = ""
            )
        ),
        isLoading = false,
        onSongClick = {},
        onBackClick = {},
        onSearchClick = {},
        onTagEditClick = { },
        onInfoClick = { _ -> },
    )
}