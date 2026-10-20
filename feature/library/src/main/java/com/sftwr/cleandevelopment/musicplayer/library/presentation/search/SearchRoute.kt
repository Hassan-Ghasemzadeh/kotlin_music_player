package com.sftwr.cleandevelopment.musicplayer.library.presentation.search


import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel.SearchUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel.SearchViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.TracksUiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SearchUiEvent.DeleteSong -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    val actions = remember(viewModel, onNavigateBack, onNavigateToPlayer) {
        SearchActions(
            onQueryChange = { viewModel.onQueryChanged(it) },
            onClearQueryClick = { viewModel.onClearQuery() },
            onFilterClick = { viewModel.onFilterClicked() },
            onCancelClick = onNavigateBack,
            onSongClick = { songId ->
                viewModel.onSongClicked(songId)
            },
            onSongMenuClick = { songId -> viewModel.onSongMenuClicked(songId) },
            onDeleteClick = { id ->
                viewModel.deleteCurrentOrSelectedSong(songId = id)
            }
        )
    }

    SearchScreen(
        uiState = uiState,
        actions = actions
    )
}