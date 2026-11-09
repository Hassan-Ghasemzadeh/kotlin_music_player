package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.AddSongToPlaylistUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetAlbumSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing tracks belonging to a specific album.
 */
@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    getAlbumSongsUseCase: GetAlbumSongsUseCase,
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val playlistRepository: PlaylistRepository,
    private val playerManager: PlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val albumId: Long = savedStateHandle.get<Long>("albumId") ?: -1L
    val albumTitle: String = savedStateHandle.get<String>("albumTitle") ?: "Album"

    private val _isLoading = MutableStateFlow(false)
    private val _targetSongForPlaylist = MutableStateFlow<SongUiModel?>(null)

    private val _toastChannel = Channel<String>()
    val toastEvent = _toastChannel.receiveAsFlow()

    val uiState: StateFlow<AlbumDetailUiState> = combine(
        getAlbumSongsUseCase(albumId),
        getPlaylistsUseCase(),
        _targetSongForPlaylist,
        _isLoading
    ) { songs, playlists, targetSong, isLoading ->
        val songUiModels = songs.map { song ->
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
        val artist = songUiModels.firstOrNull()?.artist ?: "Unknown Artist"
        AlbumDetailUiState(
            albumId = albumId,
            albumTitle = albumTitle,
            artistName = artist,
            songs = songUiModels,
            playlists = playlists,
            targetSongForPlaylist = targetSong,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumDetailUiState(albumId = albumId, albumTitle = albumTitle, isLoading = true)
    )

    fun onSongClicked(songId: String) {
        viewModelScope.launch {
            playerManager.playSongById(songId, uiState.value.songs)
        }
    }

    fun playAll() {
        val songs = uiState.value.songs
        if (songs.isNotEmpty()) {
            viewModelScope.launch {
                playerManager.setQueue(songs, startIndex = 0)
            }
        }
    }

    fun openAddToPlaylistDialog(song: SongUiModel) {
        _targetSongForPlaylist.value = song
    }

    fun closeAddToPlaylistDialog() {
        _targetSongForPlaylist.value = null
    }

    fun addSongToPlaylist(songId: Long, playlist: com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist) {
        viewModelScope.launch {
            val result = addSongToPlaylistUseCase(playlist.id, songId)
            if (result.isSuccess) {
                _toastChannel.send("Added to ${playlist.name}")
            } else {
                _toastChannel.send("Failed to add to playlist")
            }
            closeAddToPlaylistDialog()
        }
    }

    fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            playlistRepository.toggleFavorite(songId, isFavorite)
            val msg = if (isFavorite) "Added to Favorites" else "Removed from Favorites"
            _toastChannel.send(msg)
            closeAddToPlaylistDialog()
        }
    }

    fun deleteSong(songId: Long) {
        viewModelScope.launch {
            playerManager.deleteSong(songId)
        }
    }
}
