package com.sftwr.cleandevelopment.musicplayer.library.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.components.NeonSearchBar
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.components.SearchEmptyState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TrackItem

@Composable
fun SearchScreen(
    uiState: SearchUiState, actions: SearchActions, modifier: Modifier = Modifier
) {
    // Track which song's options menu is currently expanded in search results
    var expandedSongId: String? by remember { mutableStateOf<String?>(null) }

    // Main container with dark vertical gradient background matching TracksScreen
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
        Scaffold(
            modifier = modifier.fillMaxSize(),
            // Make scaffold container transparent to show the background gradient
            containerColor = Color.Transparent, topBar = {
                NeonSearchBar(
                    query = uiState.query,
                    onQueryChange = actions.onQueryChange,
                    onClearQuery = actions.onClearQueryClick,
                    onCancelClick = actions.onCancelClick
                )
            }) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center), color = Color(0xFF00D2FF)
                        )
                    }

                    uiState.query.isEmpty() -> {
                        SearchEmptyState(
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                    uiState.searchResults.isEmpty() -> {
                        Text(
                            text = "هیچ موردی یافت نشد",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        val bottomPadding = rememberPlayerAwareBottomPadding()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding
                            ), verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = uiState.searchResults, key = { it.id }) { song ->
                                val isMenuExpanded = expandedSongId == song.id
                                // Reusing the standardized glassmorphic TrackItem
                                TrackItem(
                                    song = song,
                                    onClick = {
                                        actions.onSongClick(song.id)
                                    },
                                    isMenuExpanded = isMenuExpanded,
                                    onMenuClick = {
                                        expandedSongId = song.id
                                    },
                                    onDismissMenu = {
                                        expandedSongId = null
                                    },
                                    onDeleteClick = {
                                        actions.onDeleteClick(song.id.toLong())
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}