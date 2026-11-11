package com.sftwr.cleandevelopment.musicplayer.library.presentation.artists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.AddSongToPlaylistUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetArtistAlbumsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetArtistSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ToggleArtistFavoriteUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    getArtistSongsUseCase: GetArtistSongsUseCase,
    getArtistAlbumsUseCase: GetArtistAlbumsUseCase,
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val toggleArtistFavoriteUseCase: ToggleArtistFavoriteUseCase,
    private val playlistRepository: PlaylistRepository,
    private val playerManager: PlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savedArtistName: String = savedStateHandle.get<String>("artistName") ?: "Artist"
    val artistName: String get() = uiState.value.artistName.ifBlank { savedArtistName }

    private val _isLoading = MutableStateFlow(false)
    private val _targetSongForPlaylist = MutableStateFlow<SongUiModel?>(null)

    private val _toastChannel = Channel<String>()
    val toastEvent = _toastChannel.receiveAsFlow()

    val uiState: StateFlow<ArtistDetailUiState> = combine(
        getArtistSongsUseCase(savedArtistName),
        getArtistAlbumsUseCase(savedArtistName),
        getPlaylistsUseCase(),
        _targetSongForPlaylist,
        _isLoading
    ) { songs, albums, playlists, targetSong, isLoading ->
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
        val dynamicArtistName = songUiModels.firstOrNull()?.artist?.ifBlank { savedArtistName } ?: savedArtistName
        val isArtistFavorite = songUiModels.isNotEmpty() && songUiModels.all { it.isFavorite }
        ArtistDetailUiState(
            artistName = dynamicArtistName,
            albums = albums,
            songs = songUiModels,
            playlists = playlists,
            targetSongForPlaylist = targetSong,
            isLoading = isLoading,
            isFavorite = isArtistFavorite
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArtistDetailUiState(artistName = savedArtistName, isLoading = true)
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

    fun toggleArtistFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            val result = toggleArtistFavoriteUseCase(artistName, isFavorite)
            if (result.isSuccess) {
                val msg = if (isFavorite) "Artist added to favorites" else "Artist removed from favorites"
                _toastChannel.send(msg)
            } else {
                _toastChannel.send("Failed to update artist favorites")
            }
        }
    }

    fun deleteSong(songId: Long) {
        viewModelScope.launch {
            playerManager.deleteSong(songId)
        }
    }
}
