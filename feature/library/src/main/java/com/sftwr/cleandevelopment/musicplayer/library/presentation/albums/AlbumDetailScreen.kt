package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.AddToPlaylistDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TrackItem

/**
 * AlbumDetailScreen displaying album header banner with artwork, title, artist, and track list.
 */
@Composable
fun AlbumDetailScreen(
    modifier: Modifier = Modifier,
    albumId: Long,
    albumTitle: String,
    artistName: String,
    songs: List<SongUiModel>,
    playlists: List<Playlist> = emptyList(),
    targetSongForPlaylist: SongUiModel? = null,
    isLoading: Boolean = false,
    currentPlayingSongId: String? = null,
    isPlayingGlobal: Boolean = false,
    onSongClick: (String) -> Unit,
    onPlayAllClick: () -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAddToPlaylistClick: (SongUiModel) -> Unit = {},
    onFavoriteClick: (SongUiModel) -> Unit = {},
    onSelectPlaylistForAdd: (songId: Long, Playlist) -> Unit = { _, _ -> },
    onToggleFavoriteForAdd: (songId: Long, isFavorite: Boolean) -> Unit = { _, _ -> },
    onDismissAddToPlaylistDialog: () -> Unit = {},
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit,
    onShareClick: (SongUiModel) -> Unit,
    onDeleteSongClick: (Long) -> Unit
) {
    val bottomPadding = rememberPlayerAwareBottomPadding()
    var expandedSongId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F0B21),
                            Color(0xFF181035),
                            Color(0xFF2C1454)
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
                // Top App Bar
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
                        text = albumTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Album Header Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(
                                        1.dp,
                                        Color(0xFF91D8FB).copy(alpha = 0.4f),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = "content://media/external/audio/albumart/$albumId",
                                    contentDescription = albumTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = com.sftwr.cleandevelopment.musicplayer.designsystem.R.drawable.ic_default_album_art),
                                    placeholder = painterResource(id = com.sftwr.cleandevelopment.musicplayer.designsystem.R.drawable.ic_default_album_art)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = albumTitle,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = artistName,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${songs.size} Tracks",
                                    color = Color(0xFF91D8FB).copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (songs.isNotEmpty()) {
                            Button(
                                onClick = onPlayAllClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF91D8FB),
                                    contentColor = Color(0xFF0F0B21)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play All",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Songs List
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading && songs.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF91D8FB))
                            }
                        }

                        songs.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No songs in this album",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = bottomPadding)
                            ) {
                                items(songs, key = { it.id }) { song ->
                                    val isMenuExpanded = expandedSongId == song.id
                                    val isThisSongPlaying =
                                        (currentPlayingSongId == song.id) && isPlayingGlobal
                                    val songIdLong = song.id.toLongOrNull() ?: -1L

                                    TrackItem(
                                        song = song,
                                        onClick = { onSongClick(song.id) },
                                        isMenuExpanded = isMenuExpanded,
                                        onMenuClick = { expandedSongId = song.id },
                                        onDismissMenu = { expandedSongId = null },
                                        onDeleteClick = { onDeleteSongClick(songIdLong) },
                                        onInfoClick = { onInfoClick(songIdLong) },
                                        onTagEditClick = { onTagEditClick(songIdLong) },
                                        onShareClick = { onShareClick(song) },
                                        onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                                        onFavoriteClick = { onFavoriteClick(song) },
                                        onRemoveFromPlaylist = null,
                                        isPlaying = isThisSongPlaying
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add to Playlist Dialog prompt
            if (targetSongForPlaylist != null) {
                val songIdLong = targetSongForPlaylist.id.toLongOrNull() ?: -1L
                AddToPlaylistDialog(
                    songTitle = targetSongForPlaylist.title,
                    playlists = playlists,
                    isFavorite = false,
                    onSelectPlaylist = { playlist ->
                        if (songIdLong != -1L) {
                            onSelectPlaylistForAdd(songIdLong, playlist)
                        }
                    },
                    onToggleFavorite = {
                        if (songIdLong != -1L) {
                            onToggleFavoriteForAdd(songIdLong, true)
                        }
                    },
                    onDismissRequest = onDismissAddToPlaylistDialog
                )
            }
        }
    }
}
