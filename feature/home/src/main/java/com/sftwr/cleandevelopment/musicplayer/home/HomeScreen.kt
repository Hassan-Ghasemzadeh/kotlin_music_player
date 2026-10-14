package com.sftwr.cleandevelopment.musicplayer.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sftwr.cleandevelopment.musicplayer.home.presentation.components.BottomPlayerBar
import com.sftwr.cleandevelopment.musicplayer.home.presentation.components.LibraryCard
import com.sftwr.cleandevelopment.musicplayer.home.presentation.components.PlaylistCard
import com.sftwr.cleandevelopment.musicplayer.home.presentation.components.TopBarSection

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
 * and a bottom music player bar.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    // State to control the visibility of the "Create Playlist" dialog
    var showCreateDialog by remember { mutableStateOf(false) }

    // State for the new playlist name input text
    var newPlaylistName by remember { mutableStateOf("") }

    // Mutable list of playlists to allow adding new user-created playlists dynamically
    val playlists = remember {
        mutableStateListOf(
            PlaylistUiModel("Favorite Tracks", "18 Tracks", Icons.Default.FavoriteBorder),
            PlaylistUiModel("Recently Played", "11 Tracks", Icons.Default.History),
            PlaylistUiModel("Recently added", "15 Tracks", Icons.Default.LibraryAdd),
            PlaylistUiModel("Most played", "28 Tracks", Icons.Default.TrendingUp),
            PlaylistUiModel("Downloaded Tracks", "42 Tracks", Icons.Default.Download)
        )
    }

    // Main container with deep blue gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF233E78),
                        Color(0xFF1B2F5E),
                        Color(0xFF122144)
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
            TopBarSection()

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable content for library grid and playlists
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First Row of Library Grid (Tracks, Albums, Artists)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LibraryCard(
                            title = "TRACKS",
                            subtitle = "(51)",
                            icon = Icons.Default.MusicNote,
                            modifier = Modifier.weight(1f)
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
                            text = "Playlist(${playlists.size})",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Button to trigger the playlist creation dialog
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Playlist",
                                tint = Color(0xFFFFFFFF),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }

                // Dynamic list of playlist cards
                items(playlists) { playlist ->
                    PlaylistCard(
                        title = playlist.title,
                        subtitle = playlist.subtitle,
                        icon = playlist.icon
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Player Bar
            BottomPlayerBar()
        }

        // Dialog for creating a new custom playlist
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text(text = "Create New Playlist") },
                text = {
                    Column {
                        Text(text = "Enter a name for your new playlist:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            singleLine = true,
                            placeholder = { Text("Playlist Name") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                // Add the new custom playlist to the top or bottom of the list
                                playlists.add(
                                    PlaylistUiModel(
                                        title = newPlaylistName,
                                        subtitle = "0 Tracks",
                                        icon = Icons.Default.QueueMusic
                                    )
                                )
                                newPlaylistName = ""
                                showCreateDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Preview function for Android Studio UI inspection.
 */
@Preview(showBackground = true, backgroundColor = 0xFF1B2F5E)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}