package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * UI Model representing a song item in the tracks list.
 */
data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String
)

/**
 * TracksScreen displays the list of all available audio tracks with a glassmorphic design.
 */
@Composable
fun TracksScreen(
    songs: List<SongUiModel>,
    onSongClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Main container with dark vertical gradient background matching the home screen
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
            // Top App Bar section for Tracks screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
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

            // Scrollable list of songs using LazyColumn for high performance
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(songs) { song ->
                    TrackItem(
                        song = song,
                        onClick = { onSongClick(song.id) }
                    )
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
    onClick: () -> Unit
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
                // Album art or track icon placeholder
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

                Spacer(modifier = Modifier.width(14.dp))

                // Track title and artist name
                Column {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Song duration text
                Text(
                    text = song.duration,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // More options menu button
                IconButton(
                    onClick = { /* Handle track options menu */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.7f)
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
            SongUiModel("1", "Nightfall Melody", "Aria & The Echoes", "3:45"),
            SongUiModel("2", "Midnight Drive", "Synthetics", "4:12"),
            SongUiModel("3", "Lost in Memories", "Roya", "3:08"),
            SongUiModel("4", "Silent Waves", "Ocean Breeze", "5:20")
        ),
        onSongClick = {},
        onBackClick = {},
        onSearchClick = {}
    )
}