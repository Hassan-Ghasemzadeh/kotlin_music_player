package com.sftwr.cleandevelopment.musicplayer.library.presentation.genres

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.ShareUtils
import kotlinx.coroutines.flow.collectLatest

/**
 * Stateful route wrapper for GenreDetail screen.
 */
@Composable
fun GenreDetailRoute(
    viewModel: GenreDetailViewModel = hiltViewModel(),
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSong by playerManager.currentSong.collectAsStateWithLifecycle()
    val isPlayingGlobal by playerManager.isPlaying.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    GenreDetailScreen(
        genreName = viewModel.genreName,
        songs = uiState.songs,
        playlists = uiState.playlists,
        targetSongForPlaylist = uiState.targetSongForPlaylist,
        isLoading = uiState.isLoading,
        currentPlayingSongId = currentSong?.id,
        isPlayingGlobal = isPlayingGlobal,
        onSongClick = { songId -> viewModel.onSongClicked(songId) },
        onPlayAllClick = { viewModel.playAll() },
        onBackClick = onBackClick,
        onSearchClick = onSearchClick,
        onAddToPlaylistClick = { song -> viewModel.openAddToPlaylistDialog(song) },
        onFavoriteClick = { song ->
            song.id.toLongOrNull()?.let { songId ->
                viewModel.toggleFavorite(songId, !song.isFavorite)
            }
        },
        onSelectPlaylistForAdd = { songId, playlist ->
            viewModel.addSongToPlaylist(songId, playlist)
        },
        onToggleFavoriteForAdd = { songId, isFav ->
            viewModel.toggleFavorite(songId, isFav)
        },
        onDismissAddToPlaylistDialog = {
            viewModel.closeAddToPlaylistDialog()
        },
        onInfoClick = onInfoClick,
        onTagEditClick = onTagEditClick,
        onShareClick = { song -> ShareUtils.shareSong(context, song) },
        onDeleteSongClick = { songId ->
            viewModel.deleteSong(songId)
        },
        isFavorite = uiState.isFavorite,
        onToggleGenreFavorite = { fav ->
            viewModel.toggleGenreFavorite(fav)
        }
    )
}
