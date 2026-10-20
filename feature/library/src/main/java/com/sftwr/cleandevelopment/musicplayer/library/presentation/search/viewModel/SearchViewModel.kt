package com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SearchSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.SearchUiState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
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

    private val _uiEvent = Channel<SearchUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadTracks()
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

    fun deleteCurrentOrSelectedSong(songId: Long) {
        viewModelScope.launch {
            val success = playerManager.deleteSong(songId)
            if (success) {
                _uiEvent.send(SearchUiEvent.DeleteSong("آهنگ با موفقیت حذف شد"))
            } else {
                _uiEvent.send(SearchUiEvent.DeleteSong("خطا در حذف آهنگ"))
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
     * Triggered when the filter button is clicked.
     */
    fun onFilterClicked() {
        // TODO: Implement filter logic (e.g., filter by artist, album, etc.)
    }

    /**
     * Triggered when the options menu for a specific song is clicked.
     */
    fun onSongMenuClicked(songId: String) {
        // مدیریت باز و بسته شدن منو یا انتخاب آهنگ در صورت نیاز
    }

    /**
     * Handles track selection, sets the playback queue, and triggers navigation to the player.
     */
    fun onSongClicked(songId: String) {
        val songs = _uiState.value.songs
        val index = songs.indexOfFirst { it.id == songId }
        val song = songs[index]
        // Send the entire playlist along with the selected song index to the player manager
        playerManager.playSong(song)
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

/**
 * UI events emitted from TracksViewModel for one-time actions like navigation.
 */
sealed interface SearchUiEvent {
    data class DeleteSong(val message: String) : SearchUiEvent
}