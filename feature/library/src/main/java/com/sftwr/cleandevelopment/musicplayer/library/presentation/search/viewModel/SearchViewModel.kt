package com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.AddSongToPlaylistUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.SearchUiState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing search query inputs reactively, executing song search,
 * handling audio track deletions, playlists, favorites, and mapping query result sets to UI models.
 */
@RequiresApi(Build.VERSION_CODES.S)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val playlistRepository: PlaylistRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _targetSongForPlaylist = MutableStateFlow<SongUiModel?>(null)

    private val _deleteEvent = Channel<SearchPlayerUiEvent>()
    val deleteEvent = _deleteEvent.receiveAsFlow()

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        getDeviceSongsUseCase(),
        getPlaylistsUseCase(),
        _targetSongForPlaylist
    ) { query, domainSongs, playlists, targetSong ->
        val songUiModels = domainSongs.map { song ->
            SongUiModel(
                id = song.id.toString(),
                title = song.title,
                artist = song.artist,
                duration = song.duration.formatTime(),
                uri = song.path,
                year = song.year,
                album = song.album,
                genre = song.genre,
                albumId = song.albumId,
                albumArtUri = song.albumArtUri,
                isFavorite = song.isFavorite
            )
        }

        val searchResults = if (query.isBlank()) {
            emptyList()
        } else {
            val trimmed = query.trim().lowercase()
            songUiModels.filter { song ->
                song.title.lowercase().contains(trimmed) ||
                song.artist.lowercase().contains(trimmed) ||
                song.album.lowercase().contains(trimmed)
            }
        }

        SearchUiState(
            songs = songUiModels,
            query = query,
            searchResults = searchResults,
            playlists = playlists,
            targetSongForPlaylist = targetSong,
            isLoading = false,
            isSearching = query.isNotBlank()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(isLoading = true)
    )

    private var pendingDeleteSongId: Long? = null
    private var pendingDeleteSongStringId: String? = null

    fun openAddToPlaylistDialog(song: SongUiModel) {
        _targetSongForPlaylist.value = song
    }

    fun closeAddToPlaylistDialog() {
        _targetSongForPlaylist.value = null
    }

    fun addSongToPlaylist(songId: Long, playlist: Playlist) {
        viewModelScope.launch {
            val result = addSongToPlaylistUseCase(playlist.id, songId)
            val msg = if (result.isSuccess) "Added to ${playlist.name}" else "Failed to add to playlist"
            _deleteEvent.send(SearchPlayerUiEvent.ShowToast(msg))
            closeAddToPlaylistDialog()
        }
    }

    fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            playlistRepository.toggleFavorite(songId, isFavorite)
            val msg = if (isFavorite) "Added to Favorites" else "Removed from Favorites"
            _deleteEvent.send(SearchPlayerUiEvent.ShowToast(msg))
        }
    }

    /**
     * Initiates deletion workflow for the selected track and caches its identifiers for permission handling.
     */
    fun deleteCurrentOrSelectedSong(songId: Long) {
        pendingDeleteSongId = songId
        pendingDeleteSongStringId = songId.toString()
        viewModelScope.launch {
            val result = playerManager.deleteSong(songId)
            handleDeleteResult(result)
        }
    }

    /**
     * Retries song deletion after system write/delete permission is granted by the user.
     */
    fun retryDeleteAfterPermission() {
        pendingDeleteSongId?.let { id ->
            deleteCurrentOrSelectedSong(id)
            pendingDeleteSongId = null
        }
    }

    /**
     * Refreshes search cache and re-fetches device tracks list asynchronously.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun refreshSearch() {
        // Handled reactively by getDeviceSongsUseCase flow collection
    }

    /**
     * Processes file deletion results and updates search result states or triggers permission dialogues.
     */
    private fun handleDeleteResult(result: DeleteResult) {
        viewModelScope.launch {
            when (result) {
                is DeleteResult.Success -> {
                    _deleteEvent.send(SearchPlayerUiEvent.ShowToast("آهنگ با موفقیت حذف شد"))
                    pendingDeleteSongId = null
                    pendingDeleteSongStringId = null
                }

                is DeleteResult.RequiresPermission -> {
                    _deleteEvent.send(SearchPlayerUiEvent.ShowDeletePermissionDialog(result.intentSender))
                }

                is DeleteResult.Error -> {
                    _deleteEvent.send(SearchPlayerUiEvent.ShowToast("خطا در حذف آهنگ"))
                }
            }
        }
    }

    /**
     * Handles text changes in the search input query field.
     */
    fun onQueryChanged(query: String) {
        _query.value = query
    }

    /**
     * Clears active search query input and resets search results.
     */
    fun onClearQuery() {
        _query.value = ""
    }

    /**
     * Handles search result item selection, sets active queue, and prepares playback.
     */
    fun onSongClicked(songId: String) {
        viewModelScope.launch {
            playerManager.playSongById(songId, uiState.value.songs)
        }
    }
}

/**
 * UI events related to search and file management actions.
 */
sealed interface SearchPlayerUiEvent {
    data class ShowDeletePermissionDialog(val intentSender: android.content.IntentSender) :
        SearchPlayerUiEvent

    data class ShowToast(val message: String) : SearchPlayerUiEvent
}
