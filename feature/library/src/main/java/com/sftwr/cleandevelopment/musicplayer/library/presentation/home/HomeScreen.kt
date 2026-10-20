package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.sftwr.cleandevelopment.musicplayer.library.core.permissions.rememberAudioPermissionState
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.LibraryCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.PlaylistCard
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components.TopBarSection
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.HomeUiState

/**
 * Data class representing a playlist item with title, track count, and icon.
 */
data class PlaylistUiModel(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

/**
 * MusicPlayerScreen featuring library grid, dynamic custom playlists creation,
 * and a bottom music player bar (Stateless UI Component).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    actions: HomeActions,
    tracksCount: String,
) {
    val permissionState = rememberAudioPermissionState()
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
                                        if (permissionState.status.isGranted) {
                                            actions.onNavigateToTracks()
                                        } else {
                                            permissionState.launchPermissionRequest()
                                        }
                                    },
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
                            text = "Playlist(${uiState.playlists.size})",
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

                // Dynamic list of playlist cards from ViewModel state
                items(uiState.playlists) { playlist ->
                    PlaylistCard(
                        title = playlist.title,
                        subtitle = playlist.subtitle,
                        icon = playlist.icon
                    )
                }
            }
        }

        // Dialog for creating a new custom playlist (Controlled by ViewModel state)
        if (uiState.isCreateDialogVisible) {
            AlertDialog(
                onDismissRequest = actions.onDismissDialog,
                title = { Text(text = "Create New Playlist") },
                text = {
                    Column {
                        Text(text = "Enter a name for your new playlist:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.newPlaylistName,
                            onValueChange = actions.onPlaylistNameChanged,
                            singleLine = true,
                            placeholder = { Text("Playlist Name") }
                        )
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