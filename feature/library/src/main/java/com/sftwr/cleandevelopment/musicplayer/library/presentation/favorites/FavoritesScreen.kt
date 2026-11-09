package com.sftwr.cleandevelopment.musicplayer.library.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.albums.AlbumItemCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.artists.ArtistItemCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.AddToPlaylistDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TrackItem

/**
 * Stateless screen for Favorites featuring tabs for Favorite Tracks, Favorite Albums,
 * Favorite Artists, and future placeholders for Favorite Genres.
 */
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    uiState: FavoritesUiState,
    currentPlayingSongId: String? = null,
    isPlayingGlobal: Boolean = false,
    onTabSelected: (Int) -> Unit,
    onSongClick: (String) -> Unit,
    onPlayAllClick: () -> Unit,
    onAlbumClick: (Long, String) -> Unit,
    onArtistClick: (String) -> Unit,
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
                        text = "Favorites",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs Header
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (uiState.selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTabIndex]),
                                color = Color(0xFF91D8FB)
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTabIndex == 0,
                        onClick = { onTabSelected(0) },
                        text = { Text("Tracks (${uiState.favoriteSongs.size})", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF91D8FB),
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = uiState.selectedTabIndex == 1,
                        onClick = { onTabSelected(1) },
                        text = { Text("Albums (${uiState.favoriteAlbums.size})", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF91D8FB),
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = uiState.selectedTabIndex == 2,
                        onClick = { onTabSelected(2) },
                        text = { Text("Artists (${uiState.favoriteArtists.size})", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF91D8FB),
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = uiState.selectedTabIndex == 3,
                        onClick = { onTabSelected(3) },
                        text = { Text("Genres (Coming Soon)", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF91D8FB),
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.selectedTabIndex) {
                        0 -> {
                            // Favorite Tracks Tab
                            if (uiState.favoriteSongs.isEmpty()) {
                                EmptyFavoritesState(
                                    title = "No Favorite Tracks",
                                    subtitle = "Tap the heart icon on any song to add it to your favorites."
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Play All Banner
                                    GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF5252),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "${uiState.favoriteSongs.size} Favorite Tracks",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }

                                            Button(
                                                onClick = onPlayAllClick,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF91D8FB),
                                                    contentColor = Color(0xFF0F0B21)
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play All",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "Play", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(bottom = bottomPadding)
                                    ) {
                                        items(uiState.favoriteSongs, key = { it.id }) { song ->
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
                                                isPlaying = isThisSongPlaying
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Favorite Albums Tab
                            if (uiState.favoriteAlbums.isEmpty()) {
                                EmptyFavoritesState(
                                    title = "No Favorite Albums",
                                    subtitle = "Favorite an album from the album detail screen to see it here."
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = bottomPadding)
                                ) {
                                    items(uiState.favoriteAlbums, key = { it.albumId }) { album ->
                                        AlbumItemCard(
                                            album = album,
                                            onClick = { onAlbumClick(album.albumId, album.title) }
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Favorite Artists Tab
                            if (uiState.favoriteArtists.isEmpty()) {
                                EmptyFavoritesState(
                                    title = "No Favorite Artists",
                                    subtitle = "Favorite an artist from the artist detail screen to see it here."
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = bottomPadding)
                                ) {
                                    items(uiState.favoriteArtists, key = { it.name }) { artist ->
                                        ArtistItemCard(
                                            artist = artist,
                                            onClick = { onArtistClick(artist.name) }
                                        )
                                    }
                                }
                            }
                        }

                        3 -> {
                            // Favorite Genres Placeholder Tab
                            PlaceholderTabContent(
                                title = "Favorite Genres",
                                description = "Architecture prepared for future Favorite Genres feature."
                            )
                        }
                    }
                }
            }

            // Add to Playlist Dialog prompt
            if (uiState.targetSongForPlaylist != null) {
                val songIdLong = uiState.targetSongForPlaylist.id.toLongOrNull() ?: -1L
                val customPlaylists = uiState.playlists.filterIsInstance<Playlist.Custom>()
                AddToPlaylistDialog(
                    songTitle = uiState.targetSongForPlaylist.title,
                    playlists = customPlaylists,
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

@Composable
fun EmptyFavoritesState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFFF5252).copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlaceholderTabContent(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF91D8FB)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
