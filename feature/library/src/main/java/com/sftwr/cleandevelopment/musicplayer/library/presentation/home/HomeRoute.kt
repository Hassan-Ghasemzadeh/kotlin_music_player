package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.HomeUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.HomeViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel.PlaylistsViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    playlistsViewModel: PlaylistsViewModel = hiltViewModel(),
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToTracks: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToArtists: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onOpenPlaylistDetail: (id: Long, title: String, typeName: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    // Collect UI state safely with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlistsState by playlistsViewModel.uiState.collectAsStateWithLifecycle()
    val trackCount by viewModel.trackCount.collectAsStateWithLifecycle()
    val albumCount by viewModel.albumCount.collectAsStateWithLifecycle()
    val artistCount by viewModel.artistCount.collectAsStateWithLifecycle()
    val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()

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
        playlistsState = playlistsState,
        actions = HomeActions(
            onPlaylistClick = { viewModel.onPlaylistClicked(it) },
            onPlaylistCardClick = { id, title, typeName ->
                onOpenPlaylistDetail(id, title, typeName)
            },
            onSearchClick = { viewModel.onSearchClicked() },
            onSongClick = { viewModel.onSongPlayClicked(it) },
            onAddPlaylistClick = { playlistsViewModel.onAddPlaylistClick() },
            onDismissDialog = { playlistsViewModel.onDismissDialog() },
            onPlaylistNameChanged = { playlistsViewModel.onPlaylistNameChanged(it) },
            onProceedToTrackSelection = { playlistsViewModel.onProceedToTrackSelection() },
            onToggleSongSelection = { songId -> playlistsViewModel.onToggleSongSelection(songId) },
            onToggleSelectAll = { songIds -> playlistsViewModel.onToggleSelectAll(songIds) },
            onTrackSearchQueryChanged = { query -> playlistsViewModel.onTrackSearchQueryChanged(query) },
            onConfirmCreate = { playlistsViewModel.confirmCreatePlaylist() },
            onNavigateToTracks = onNavigateToTracks,
            onNavigateToAlbums = onNavigateToAlbums,
            onNavigateToArtists = onNavigateToArtists,
            onNavigateToFavorites = onNavigateToFavorites,
            onPermissionGranted = { viewModel.syncDeviceSongs() },
        ),
        modifier = modifier,
        tracksCount = trackCount.toString(),
        albumsCount = albumCount.toString(),
        artistsCount = artistCount.toString(),
        favoriteCount = favoriteCount.toString()
    )
}
