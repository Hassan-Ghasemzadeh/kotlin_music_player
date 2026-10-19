package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.HomeUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToTracks: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect UI state safely with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val trackCount by viewModel.trackCount.collectAsStateWithLifecycle()

    // Handle one-time UI events (Navigation side-effects)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is HomeUiEvent.NavigateToPlayer -> onNavigateToPlayer(event.songId)
                is HomeUiEvent.NavigateToSearch -> onNavigateToSearch()
                is HomeUiEvent.OpenPlaylist -> onOpenPlaylist(event.playlistTitle)
            }
        }
    }

    // Pass state and action callbacks down to the stateless UI screen
    HomeScreen(
        uiState = uiState,
        actions = HomeActions(
            onPlaylistClick = { viewModel.onPlaylistClicked(it) },
            onSearchClick = { viewModel.onSearchClicked() },
            onSongClick = { viewModel.onSongPlayClicked(it) },
            onAddPlaylistClick = { viewModel.onAddPlaylistClicked() },
            onDismissDialog = { viewModel.onDismissCreateDialog() },
            onPlaylistNameChanged = { viewModel.onNewPlaylistNameChanged(it) },
            onConfirmCreate = { viewModel.confirmCreatePlaylist() },
            onNavigateToTracks = onNavigateToTracks,
        ),
        modifier = modifier,
        tracksCount = trackCount.toString(),
    )
}