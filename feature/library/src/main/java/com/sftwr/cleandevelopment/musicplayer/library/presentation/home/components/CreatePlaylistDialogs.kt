package com.sftwr.cleandevelopment.musicplayer.library.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

/**
 * Step 1: Dark Glassmorphic Dialog to prompt the user for a new playlist name.
 */
@Composable
fun CreatePlaylistNameDialog(
    playlistName: String,
    nameError: String?,
    onNameChanged: (String) -> Unit,
    onNextClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F0B21),
                                Color(0xFF181035),
                                Color(0xFF2C1454)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Create New Playlist",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter a title for your playlist",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = onNameChanged,
                        singleLine = true,
                        isError = nameError != null,
                        placeholder = {
                            Text(
                                "My Favorite Songs",
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF91D8FB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            cursorColor = Color(0xFF91D8FB),
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (nameError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nameError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismissRequest) {
                            Text(
                                text = "Cancel",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = onNextClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF91D8FB),
                                contentColor = Color(0xFF0F0B21)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Next",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 2: Dark Glassmorphic Multi-Selection Dialog displaying device tracks with checkboxes.
 */
@Composable
fun TrackSelectionDialog(
    playlistName: String,
    availableSongs: List<SongUiModel>,
    selectedSongIds: Set<Long>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onToggleSongSelection: (Long) -> Unit,
    onToggleSelectAll: (List<Long>) -> Unit,
    onSavePlaylistClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
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
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Dialog Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Songs",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$playlistName • ${selectedSongIds.size} selected",
                                color = Color(0xFF91D8FB),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search & Select All Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.05f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChanged,
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF91D8FB)),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search tracks...",
                                                    color = Color.White.copy(alpha = 0.4f),
                                                    fontSize = 14.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChanged("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        val allVisibleIds = availableSongs.mapNotNull { it.id.toLongOrNull() }
                        val isAllSelected = allVisibleIds.isNotEmpty() && selectedSongIds.containsAll(allVisibleIds)

                        TextButton(
                            onClick = { onToggleSelectAll(allVisibleIds) }
                        ) {
                            Text(
                                text = if (isAllSelected) "Deselect" else "Select All",
                                color = Color(0xFF91D8FB),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Songs List with Checkboxes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (availableSongs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tracks available",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableSongs, key = { it.id }) { song ->
                                    val songId = song.id.toLongOrNull() ?: return@items
                                    val isSelected = selectedSongIds.contains(songId)

                                    TrackSelectionItem(
                                        song = song,
                                        isSelected = isSelected,
                                        onClick = { onToggleSongSelection(songId) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dialog Footer / Confirm Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismissRequest) {
                            Text(
                                text = "Cancel",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = onSavePlaylistClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF91D8FB),
                                contentColor = Color(0xFF0F0B21)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Save Playlist (${selectedSongIds.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual track row in the selection dialog.
 */
@Composable
private fun TrackSelectionItem(
    song: SongUiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF91D8FB).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF91D8FB).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox affordance
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF91D8FB) else Color.White.copy(alpha = 0.1f)
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color(0xFF91D8FB) else Color.White.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF0F0B21),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Music Note Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF91D8FB) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Track details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isSelected) Color(0xFF91D8FB) else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = song.artist,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = song.duration,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Preview
@Composable
fun TrackSelectionDialogPreview() {
    TrackSelectionDialog(
        playlistName = "Moein",
        availableSongs = listOf(
            SongUiModel(id = "1", title = "Akhe Fereshteh Ham...", artist = "Ebi,Kamran,Human", album = "", year = "", genre = "", duration = "04:26", uri = ""),
            SongUiModel(id = "2", title = "Andak andak", artist = "موزیکدل", album = "", year = "", genre = "", duration = "04:23", uri = "")
        ),
        selectedSongIds = emptySet(),
        searchQuery = "",
        onSearchQueryChanged = {},
        onToggleSongSelection = {},
        onToggleSelectAll = {},
        onSavePlaylistClick = {},
        onDismissRequest = {}
    )
}

