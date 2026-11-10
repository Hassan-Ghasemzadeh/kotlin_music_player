package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.sftwr.cleandevelopment.musicplayer.library.core.permissions.rememberAppPermissionsState
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.CreatePlaylistNameDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.LibraryCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.PlaylistCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.TopBarSection
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.TrackSelectionDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.HomeUiState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.PlaylistsUiState

/**
 * MusicPlayerScreen featuring library grid, dynamic default & custom playlists,
 * and dark glassmorphic playlist creation & track selection dialogs (Stateless UI Component).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    playlistsState: PlaylistsUiState = PlaylistsUiState(),
    actions: HomeActions,
    tracksCount: String,
    albumsCount: String,
    artistsCount: String,
    favoriteCount: String = "0",
    foldersCount: String = "0",
    genresCount: String = "0",
) {
    val permissionsState = rememberAppPermissionsState()

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    val isAudioPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsState.permissions.find { it.permission == Manifest.permission.READ_MEDIA_AUDIO }?.status?.isGranted == true
    } else {
        permissionsState.permissions.find { it.permission == Manifest.permission.READ_EXTERNAL_STORAGE }?.status?.isGranted == true
    }

    LaunchedEffect(isAudioPermissionGranted) {
        if (isAudioPermissionGranted) {
            actions.onPermissionGranted()
        }
    }

    val bottomPadding = rememberPlayerAwareBottomPadding()
    Box(
        modifier = modifier
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top App Bar with Menu, Title, and Search icons
            TopBarSection(
                onSearchClick = actions.onSearchClick,
                onMenuClick = actions.onMenuClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable content for library grid and playlists
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    bottom = bottomPadding
                )
            ) {
                // First Row of Library Grid (Tracks, Albums, Artists)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LibraryCard(
                            title = "TRACKS",
                            subtitle = "($tracksCount)",
                            icon = Icons.Default.MusicNote,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        if (isAudioPermissionGranted) {
                                            actions.onNavigateToTracks()
                                        } else {
                                            permissionsState.launchMultiplePermissionRequest()
                                        }
                                    }
                                )
                        )
                        LibraryCard(
                            title = "ALBUMS",
                            subtitle = "($albumsCount)",
                            icon = Icons.Default.Album,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        if (isAudioPermissionGranted) {
                                            actions.onNavigateToAlbums()
                                        } else {
                                            permissionsState.launchMultiplePermissionRequest()
                                        }
                                    }
                                )
                        )
                        LibraryCard(
                            title = "ARTISTS",
                            subtitle = "($artistsCount)",
                            icon = Icons.Default.Person,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        if (isAudioPermissionGranted) {
                                            actions.onNavigateToArtists()
                                        } else {
                                            permissionsState.launchMultiplePermissionRequest()
                                        }
                                    }
                                )
                        )
                    }
                }

                // Second Row of Library Grid (Folders, Genres, Favorites)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LibraryCard(
                            title = "FOLDERS",
                            subtitle = "($foldersCount)",
                            icon = Icons.Default.Folder,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        if (isAudioPermissionGranted) {
                                            actions.onNavigateToFolders()
                                        } else {
                                            permissionsState.launchMultiplePermissionRequest()
                                        }
                                    }
                                )
                        )
                        LibraryCard(
                            title = "GENRES",
                            subtitle = "($genresCount)",
                            icon = Icons.Default.MusicVideo,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        if (isAudioPermissionGranted) {
                                            actions.onNavigateToGenres()
                                        } else {
                                            permissionsState.launchMultiplePermissionRequest()
                                        }
                                    }
                                )
                        )
                        LibraryCard(
                            title = "FAVORITES",
                            subtitle = "($favoriteCount)",
                            icon = Icons.Default.Favorite,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        if (isAudioPermissionGranted) {
                                            actions.onNavigateToFavorites()
                                        } else {
                                            permissionsState.launchMultiplePermissionRequest()
                                        }
                                    }
                                )
                        )
                    }
                }

                // Playlist Section Header with Add New Playlist Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playlist(${playlistsState.totalPlaylistCount})",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Button to trigger the playlist creation dialog in ViewModel
                        IconButton(onClick = actions.onAddPlaylistClick) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Playlist",
                                tint = Color(0xFFFFFFFF),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }

                // Dynamic list of playlist cards (Default playlists + Custom user playlists)
                items(playlistsState.playlists, key = { it.id }) { playlist ->
                    PlaylistCard(
                        title = playlist.title,
                        subtitle = playlist.subtitle,
                        icon = playlist.icon,
                        modifier = Modifier.clickable {
                            actions.onPlaylistCardClick(playlist.id, playlist.title, playlist.type.name)
                        }
                    )
                }
            }
        }

        // Step 1: Glassmorphic Dialog to enter Playlist Name
        if (playlistsState.isNameDialogVisible) {
            CreatePlaylistNameDialog(
                playlistName = playlistsState.newPlaylistName,
                nameError = playlistsState.nameError,
                onNameChanged = actions.onPlaylistNameChanged,
                onNextClick = actions.onProceedToTrackSelection,
                onDismissRequest = actions.onDismissDialog
            )
        }

        // Step 2: Dark Glassmorphic Multi-Selection Dialog for choosing tracks
        if (playlistsState.isTrackSelectionVisible) {
            TrackSelectionDialog(
                playlistName = playlistsState.newPlaylistName,
                availableSongs = playlistsState.availableSongs,
                selectedSongIds = playlistsState.selectedSongIds,
                searchQuery = playlistsState.trackSearchQuery,
                onSearchQueryChanged = actions.onTrackSearchQueryChanged,
                onToggleSongSelection = actions.onToggleSongSelection,
                onToggleSelectAll = actions.onToggleSelectAll,
                onSavePlaylistClick = actions.onConfirmCreate,
                onDismissRequest = actions.onDismissDialog
            )
        }
    }
}
