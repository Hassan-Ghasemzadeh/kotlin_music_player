package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractExtraMetadata
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playerManager: PlayerManager,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var scrollIndex: Int
        get() = savedStateHandle.get<Int>("scroll_index") ?: 0
        set(value) {
            savedStateHandle["scroll_index"] = value
        }

    var scrollOffset: Int
        get() = savedStateHandle.get<Int>("scroll_offset") ?: 0
        set(value) {
            savedStateHandle["scroll_offset"] = value
        }

    private val _isLoading = MutableStateFlow(true)
    private val _isRefreshing = MutableStateFlow(false)

    // دریافت مستقیم و واکنش‌گرا از کش محلی (Room) و ترکیب با وضعیت بارگذاری
    val uiState: StateFlow<TracksUiState> = combine(
        musicRepository.getDeviceSongs(),
        _isLoading,
        _isRefreshing
    ) { domainSongs, isLoading, isRefreshing ->
        val songUiModels = withContext(Dispatchers.IO) {
            domainSongs.map { song ->
                val extraMetadata = song.path.extractExtraMetadata()
                SongUiModel(
                    id = song.id.toString(),
                    title = song.title,
                    artist = song.artist,
                    duration = song.duration.formatTime(),
                    uri = song.path,
                    year = extraMetadata.year ?: "",
                    album = song.album,
                    genre = extraMetadata.genre ?: "",
                )
            }
        }
        playerManager.setSongs(songUiModels)

        TracksUiState(
            songs = songUiModels,
            isLoading = isLoading,
            isRefreshing = isRefreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TracksUiState(isLoading = true)
    )

    private val _uiEvent = Channel<TracksUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _deleteEvent = Channel<PlayerUiEvent>()
    val deleteEvent = _deleteEvent.receiveAsFlow()

    val currentPlayingSong = playerManager.currentSong
    val isPlayerPlaying = playerManager.isPlaying

    init {
        syncTracks()
    }

    private var pendingDeleteSongId: Long? = null

    fun deleteCurrentOrSelectedSong(songId: Long) {
        pendingDeleteSongId = songId
        viewModelScope.launch {
            val result = playerManager.deleteSong(songId)
            handleDeleteResult(result)
        }
    }

    fun retryDeleteAfterPermission() {
        pendingDeleteSongId?.let { id ->
            deleteCurrentOrSelectedSong(id)
            pendingDeleteSongId = null
        }
    }


    fun onAppResumed() {
        viewModelScope.launch {
            musicRepository.syncWithDevice()
        }
    }

    private fun syncTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            musicRepository.syncWithDevice()
            _isLoading.value = false
        }
    }

    fun onSongClicked(songId: String) {
        val songs = uiState.value.songs
        val index = songs.indexOfFirst { it.id == songId }

        if (index != -1) {
            playerManager.setQueue(songs, startIndex = index)
        }
    }

    private fun handleDeleteResult(result: DeleteResult) {
        viewModelScope.launch {
            when (result) {
                is DeleteResult.Success -> {
                    _deleteEvent.send(PlayerUiEvent.ShowToast("آهنگ با موفقیت حذف شد"))
                }

                is DeleteResult.RequiresPermission -> {
                    _deleteEvent.send(PlayerUiEvent.ShowDeletePermissionDialog(result.intentSender))
                }

                is DeleteResult.Error -> {
                    _deleteEvent.send(PlayerUiEvent.ShowToast("خطا در حذف آهنگ"))
                }
            }
        }
    }
}

sealed interface TracksUiEvent {
    object NavigateToSearch : TracksUiEvent
}

sealed interface PlayerUiEvent {
    data class ShowDeletePermissionDialog(val intentSender: android.content.IntentSender) :
        PlayerUiEvent

    data class ShowToast(val message: String) : PlayerUiEvent
}