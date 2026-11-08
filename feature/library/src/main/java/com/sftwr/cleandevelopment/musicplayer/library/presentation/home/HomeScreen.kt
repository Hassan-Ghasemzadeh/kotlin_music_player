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
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.LibraryCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.PlaylistCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.TopBarSection
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.HomeUiState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.PlaylistsUiState

/**
 * MusicPlayerScreen featuring library grid, dynamic default & custom playlists,
 * and playlist creation dialog (Stateless UI Component).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    playlistsState: PlaylistsUiState = PlaylistsUiState(),
    actions: HomeActions,
    tracksCount: String,
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
                onSearchClick = actions.onSearchClick
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
                            subtitle = "(39)",
                            icon = Icons.Default.Album,
                            modifier = Modifier.weight(1f)
                        )
                        LibraryCard(
                            title = "ARTISTS",
                            subtitle = "(31)",
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Second Row of Library Grid (Folders, Genres, Videos)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LibraryCard(
                            title = "FOLDERS",
                            subtitle = "(2)",
                            icon = Icons.Default.Folder,
                            modifier = Modifier.weight(1f)
                        )
                        LibraryCard(
                            title = "GENRES",
                            subtitle = "(13)",
                            icon = Icons.Default.MusicVideo,
                            modifier = Modifier.weight(1f)
                        )
                        LibraryCard(
                            title = "VIDEOS",
                            subtitle = "(8)",
                            icon = Icons.Default.Videocam,
                            modifier = Modifier.weight(1f)
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
                        modifier = Modifier.clickable { actions.onPlaylistClick(playlist.title) }
                    )
                }
            }
        }

        // Dialog for creating a new custom playlist (Controlled by ViewModel state)
        if (playlistsState.isCreateDialogVisible) {
            AlertDialog(
                onDismissRequest = actions.onDismissDialog,
                title = { Text(text = "Create New Playlist") },
                text = {
                    Column {
                        Text(text = "Enter a name for your new playlist:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = playlistsState.newPlaylistName,
                            onValueChange = actions.onPlaylistNameChanged,
                            singleLine = true,
                            isError = playlistsState.nameError != null,
                            placeholder = { Text("Playlist Name") }
                        )
                        if (playlistsState.nameError != null) {
                            Text(
                                text = playlistsState.nameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = actions.onConfirmCreate) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = actions.onDismissDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
