package com.sftwr.cleandevelopment.musicplayer.library.presentation.favorites

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
 * Stateful route wrapper for the Favorites screen.
 */
@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel = hiltViewModel(),
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAlbumClick: (Long, String) -> Unit,
    onArtistClick: (String) -> Unit,
    onGenreClick: (String) -> Unit = {},
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

    FavoritesScreen(
        uiState = uiState,
        currentPlayingSongId = currentSong?.id,
        isPlayingGlobal = isPlayingGlobal,
        onTabSelected = { index -> viewModel.onTabSelected(index) },
        onSongClick = { songId -> viewModel.onSongClicked(songId) },
        onPlayAllClick = { viewModel.playAllFavoriteSongs() },
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        onGenreClick = onGenreClick,
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
        }
    )
}
