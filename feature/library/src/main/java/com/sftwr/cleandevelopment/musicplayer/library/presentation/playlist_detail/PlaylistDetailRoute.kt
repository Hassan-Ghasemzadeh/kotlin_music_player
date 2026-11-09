package com.sftwr.cleandevelopment.musicplayer.library.presentation.playlist_detail

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.ShareUtils
import com.sftwr.cleandevelopment.musicplayer.library.presentation.playlist_detail.viewmodel.PlaylistDetailViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PlaylistDetailRoute(
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit,
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

    val deletePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Delete permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    PlaylistDetailScreen(
        playlistTitle = uiState.playlistName,
        playlistType = uiState.playlistType,
        songs = uiState.songs,
        playlists = uiState.playlists,
        targetSongForPlaylist = uiState.targetSongForPlaylist,
        isLoading = uiState.isLoading,
        currentPlayingSongId = currentSong?.id,
        isPlayingGlobal = isPlayingGlobal,
        isCustomPlaylist = uiState.isCustomPlaylist,
        onSongClick = { songId -> viewModel.onSongClicked(songId) },
        onPlayAllClick = { viewModel.playAll() },
        onBackClick = onBackClick,
        onSearchClick = onNavigateToSearch,
        onDeletePlaylistClick = if (uiState.isCustomPlaylist) {
            { viewModel.deletePlaylist(onDeleted = onBackClick) }
        } else null,
        onRemoveFromPlaylistClick = if (uiState.isCustomPlaylist) {
            { songId -> viewModel.removeSongFromPlaylist(songId) }
        } else null,
        onAddToPlaylistClick = { song -> viewModel.openAddToPlaylistDialog(song) },
        onFavoriteClick = { song ->
            song.id.toLongOrNull()?.let { songId ->
                viewModel.toggleFavorite(songId, true)
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
        onToggleFavoriteClick = { songId ->
            viewModel.toggleFavorite(songId, true)
        },
        onInfoClick = onInfoClick,
        onTagEditClick = onTagEditClick,
        onShareClick = { song -> ShareUtils.shareSong(context, song) },
        onDeleteSongClick = { songId -> viewModel.deleteSongFromDevice(songId) }
    )
}
