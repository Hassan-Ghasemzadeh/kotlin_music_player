package com.sftwr.cleandevelopment.musicplayer.library.presentation.playlist_detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.AddSongToPlaylistUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistSongsUseCase
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
class PlaylistDetailViewModel @Inject constructor(
    private val getPlaylistSongsUseCase: GetPlaylistSongsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val playlistRepository: PlaylistRepository,
    private val playerManager: PlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: -1L
    val playlistName: String = savedStateHandle.get<String>("playlistName") ?: "Playlist"
    private val playlistTypeString: String = savedStateHandle.get<String>("playlistTypeString") ?: PlaylistType.CUSTOM.name

    val playlistType: PlaylistType = try {
        PlaylistType.valueOf(playlistTypeString)
    } catch (_: Exception) {
        PlaylistType.CUSTOM
    }

    private val _isLoading = MutableStateFlow(false)
    private val _targetSongForPlaylist = MutableStateFlow<SongUiModel?>(null)

    private val _toastChannel = Channel<String>()
    val toastEvent = _toastChannel.receiveAsFlow()

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        getPlaylistSongsUseCase(playlistId, playlistType),
        getPlaylistsUseCase(),
        _targetSongForPlaylist,
        _isLoading
    ) { domainSongs, playlists, targetSong, isLoading ->
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
                isFavorite = song.isFavorite,
            )
        }
        PlaylistDetailUiState(
            playlistId = playlistId,
            playlistName = playlistName,
            playlistType = playlistType,
            songs = songUiModels,
            playlists = playlists,
            customPlaylists = playlists.filter { it is Playlist.Custom },
            targetSongForPlaylist = targetSong,
            isLoading = isLoading,
            isCustomPlaylist = playlistType == PlaylistType.CUSTOM
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistDetailUiState(
            playlistId = playlistId,
            playlistName = playlistName,
            playlistType = playlistType,
            isLoading = true,
            isCustomPlaylist = playlistType == PlaylistType.CUSTOM
        )
    )

    fun openAddToPlaylistDialog(song: SongUiModel) {
        _targetSongForPlaylist.value = song
    }

    fun closeAddToPlaylistDialog() {
        _targetSongForPlaylist.value = null
    }

    fun addSongToPlaylist(songId: Long, playlist: Playlist) {
        viewModelScope.launch {
            val result = addSongToPlaylistUseCase(playlist.id, songId)
            if (result.isSuccess) {
                _toastChannel.send("به لیست ${playlist.name} اضافه شد")
            } else {
                _toastChannel.send("خطا در افزودن به لیست پخش")
            }
            closeAddToPlaylistDialog()
        }
    }

    fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            playlistRepository.toggleFavorite(songId, isFavorite)
            val msg = if (isFavorite) "به لیست دلخواه اضافه شد" else "از لیست دلخواه حذف شد"
            _toastChannel.send(msg)
            closeAddToPlaylistDialog()
        }
    }

    fun onSongClicked(songId: String) {
        viewModelScope.launch {
            val songs = uiState.value.songs
            playerManager.playSongById(songId, songs)
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

    fun removeSongFromPlaylist(songId: Long) {
        if (playlistType == PlaylistType.CUSTOM) {
            viewModelScope.launch {
                playlistRepository.removeSongFromPlaylist(playlistId, songId)
            }
        }
    }

    fun deletePlaylist(onDeleted: () -> Unit) {
        if (playlistType == PlaylistType.CUSTOM) {
            viewModelScope.launch {
                playlistRepository.deleteCustomPlaylist(playlistId)
                onDeleted()
            }
        }
    }

    fun deleteSongFromDevice(songId: Long) {
        viewModelScope.launch {
            playerManager.deleteSong(songId)
        }
    }
}
