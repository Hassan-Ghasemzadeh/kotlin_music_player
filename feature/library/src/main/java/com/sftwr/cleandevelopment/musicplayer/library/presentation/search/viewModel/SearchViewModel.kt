package com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SearchSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractExtraMetadata
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.SearchUiState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel responsible for managing search query inputs, executing song search use cases,
 * handling audio track deletions, and mapping query result sets to UI models.
 */
@RequiresApi(Build.VERSION_CODES.S)
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
    private var pendingDeleteSongStringId: String? = null

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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val rawResults = getDeviceSongsUseCase().firstOrNull()

                if (rawResults != null) {
                    val mappedResults = rawResults.map { song ->
                        val extraMetadata = withContext(Dispatchers.IO) {
                            song.path.extractExtraMetadata()
                        }
                        SongUiModel(
                            id = song.id.toString(),
                            title = song.title,
                            artist = song.artist,
                            duration = song.duration.formatTime(),
                            uri = song.path,
                            year = extraMetadata.year ?: "",
                            album = song.path,
                            genre = extraMetadata.genre ?: "",
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSearching = true,
                            searchResults = mappedResults
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        searchResults = emptyList()
                    )
                }
            }
        }
    }

    /**
     * Fetches raw device songs from domain use case and populates base song models.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getDeviceSongsUseCase().collect { domainSongs ->
                val songUiModels = domainSongs.map { song ->
                    val extraMetadata = withContext(Dispatchers.IO) {
                        song.path.extractExtraMetadata()
                    }
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path,
                        year = extraMetadata.year ?: "",
                        album = song.path,
                        genre = extraMetadata.genre ?: "",
                    )
                }

                _uiState.update {
                    it.copy(
                        songs = songUiModels,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Processes file deletion results and updates search result states or triggers permission dialogues.
     */
    private fun handleDeleteResult(result: DeleteResult) {
        viewModelScope.launch {
            when (result) {
                is DeleteResult.Success -> {
                    _deleteEvent.send(SearchPlayerUiEvent.ShowToast("آهنگ با موفقیت حذف شد"))

                    // Filter out the deleted song from search results state
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
     * Handles text changes in the search input query field.
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
     * Clears active search query input and resets search results.
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
     * Handles search result item selection, sets active queue, and prepares playback.
     */
    fun onSongClicked(songId: String) {
        val songs = _uiState.value.searchResults
        val index = songs.indexOfFirst { it.id == songId }

        if (index != -1) {
            playerManager.setQueue(songs, startIndex = index)
        }
    }

    /**
     * Executes the search query using search use-cases and maps matching results on IO thread pool.
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val rawResults = searchSongsUseCase(query)

                val mappedResults = rawResults.map { song ->
                    val extraMetadata = withContext(Dispatchers.IO) {
                        song.path.extractExtraMetadata()
                    }
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path,
                        year = extraMetadata.year ?: "",
                        album = song.path,
                        genre = extraMetadata.genre ?: "",
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSearching = true,
                        searchResults = mappedResults
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
 * UI events related to search and file management actions.
 */
sealed interface SearchPlayerUiEvent {
    data class ShowDeletePermissionDialog(val intentSender: android.content.IntentSender) :
        SearchPlayerUiEvent

    data class ShowToast(val message: String) : SearchPlayerUiEvent
}