package com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SearchSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.SearchUiState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.PlayerUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.TracksUiEvent
// import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _deleteEvent = Channel<SearchPlayerUiEvent>()
    val deleteEvent = _deleteEvent.receiveAsFlow()

    init {
        loadTracks()
    }

    private var pendingDeleteSongId: Long? = null
    private var pendingDeleteSongStringId: String? =
        null // ذخیره به صورت استرینگ برای مقایسه راحت‌تر

    fun deleteCurrentOrSelectedSong(songId: Long) {
        pendingDeleteSongId = songId
        pendingDeleteSongStringId = songId.toString() // ذخیره دقیق استرینگ آیدی
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

    /**
     * Fetches actual device songs from the UseCase and maps them to SongUiModel.
     */
    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getDeviceSongsUseCase().collect { domainSongs ->
                val songUiModels = domainSongs.map { song ->
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path
                    )
                }

                _uiState.value = _uiState.value.copy(
                    songs = songUiModels,
                    isLoading = false
                )
            }
        }
    }

    private fun handleDeleteResult(result: DeleteResult) {
        viewModelScope.launch {
            when (result) {
                is DeleteResult.Success -> {
                    _deleteEvent.send(SearchPlayerUiEvent.ShowToast("آهنگ با موفقیت حذف شد"))
                    _uiState.update { currentState ->
                        val updatedResults =
                            currentState.searchResults.filter { it.id != pendingDeleteSongId?.toString() }
                        currentState.copy(searchResults = updatedResults)
                    }

                    _uiState.update { currentState ->
                        val targetId = pendingDeleteSongStringId
                        val updatedResults = currentState.searchResults.filter { it.id != targetId }
                        currentState.copy(searchResults = updatedResults)
                    }

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
     * Handles changes in the search query text input.
     */
    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query, isLoading = query.isNotBlank()) }

        if (query.isNotBlank()) {
            performSearch(query)
        } else {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
        }
    }

    /**
     * Clears the current search query and results.
     */
    fun onClearQuery() {
        _uiState.update {
            it.copy(
                query = "",
                searchResults = emptyList(),
                isLoading = false,
                isSearching = false
            )
        }
    }


    /**
     * Handles track selection, sets the playback queue, and triggers navigation to the player.
     */
    fun onSongClicked(songId: String) {
        val songs = _uiState.value.searchResults
        val index = songs.indexOfFirst { it.id == songId }

        if (index != -1) {
            // Send the entire playlist along with the selected song index to the player manager
            playerManager.setQueue(songs, startIndex = index)
        }
    }

    /**
     * Performs the actual search operation and maps results to SongUiModel.
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // ۱. صدا زدن UseCase و گرفتن لیست Song (دامین)
                val rawResults = searchSongsUseCase(query)

                // ۲. تبدیل Song به SongUiModel برای نمایش در لایه UI
                val mappedResults = rawResults.map { song ->
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        uri = song.path,
                        duration = song.duration.formatTime()
                    )
                }

                // ۳. به‌روزرسانی استیت صفحه
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSearching = true,
                        searchResults = mappedResults
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        searchResults = emptyList()
                    )
                }
            }
        }
    }
}

sealed interface SearchPlayerUiEvent {
    data class ShowDeletePermissionDialog(val intentSender: android.content.IntentSender) :
        SearchPlayerUiEvent

    data class ShowToast(val message: String) : SearchPlayerUiEvent
}