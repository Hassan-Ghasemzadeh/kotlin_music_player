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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.home.R
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.AddToPlaylistDialog
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.components.NeonSearchBar
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.components.SearchEmptyState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TrackItem

/**
 * SearchScreen stateless composable responsible for rendering the search bar input,
 * handling loading states, and displaying matching audio tracks using glassmorphic list items.
 */
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    actions: SearchActions,
    onAddToPlaylistClick: (SongUiModel) -> Unit = {},
    onFavoriteClick: (SongUiModel) -> Unit = {},
    onSelectPlaylistForAdd: (songId: Long, Playlist) -> Unit = { _, _ -> },
    onToggleFavoriteForAdd: (songId: Long, isFavorite: Boolean) -> Unit = { _, _ -> },
    onDismissAddToPlaylistDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expandedSongId: String? by remember { mutableStateOf(null) }

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
            containerColor = Color.Transparent,
            topBar = {
                NeonSearchBar(
                    query = uiState.query,
                    onQueryChange = actions.onQueryChange,
                    onClearQuery = actions.onClearQueryClick,
                    onCancelClick = actions.onCancelClick
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF00D2FF)
                        )
                    }

                    uiState.query.isEmpty() -> {
                        SearchEmptyState(
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                    uiState.searchResults.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.no_results_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        val bottomPadding = rememberPlayerAwareBottomPadding()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.searchResults,
                                key = { it.id }
                            ) { song ->
                                TrackItem(
                                    song = song,
                                    onClick = { actions.onSongClick(song.id) },
                                    isMenuExpanded = expandedSongId == song.id,
                                    onMenuClick = {
                                        expandedSongId = if (expandedSongId == song.id) null else song.id
                                    },
                                    onDismissMenu = { expandedSongId = null },
                                    onDeleteClick = { song.id.toLongOrNull()?.let { id -> actions.onDeleteClick(id) } },
                                    onInfoClick = { song.id.toLongOrNull()?.let { id -> actions.onInfoClick(id) } },
                                    onTagEditClick = { song.id.toLongOrNull()?.let { id -> actions.onTagEditClick(id) } },
                                    onShareClick = { actions.onShareClick(song) },
                                    onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                                    onFavoriteClick = { onFavoriteClick(song) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
