package com.sftwr.cleandevelopment.musicplayer.library.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.*
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing favorite tracks, favorite albums, favorite artists, and favorite genres.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getPlaylistSongsUseCase: GetPlaylistSongsUseCase,
    getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase,
    getFavoriteArtistsUseCase: GetFavoriteArtistsUseCase,
    getFavoriteGenresUseCase: GetFavoriteGenresUseCase,
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val playlistRepository: PlaylistRepository,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    private val _targetSongForPlaylist = MutableStateFlow<SongUiModel?>(null)
    private val _isLoading = MutableStateFlow(false)

    private val _toastChannel = Channel<String>()
    val toastEvent = _toastChannel.receiveAsFlow()

    val uiState: StateFlow<FavoritesUiState> = combine(
        _selectedTabIndex,
        getPlaylistSongsUseCase(-4L, PlaylistType.FAVORITES),
        getFavoriteAlbumsUseCase(),
        getFavoriteArtistsUseCase(),
        getFavoriteGenresUseCase(),
        getPlaylistsUseCase(),
        _targetSongForPlaylist,
        _isLoading
    ) { args ->
        val tabIndex = args[0] as Int
        @Suppress("UNCHECKED_CAST")
        val songs = args[1] as List<Song>
        @Suppress("UNCHECKED_CAST")
        val albums = args[2] as List<Album>
        @Suppress("UNCHECKED_CAST")
        val artists = args[3] as List<Artist>
        @Suppress("UNCHECKED_CAST")
        val genres = args[4] as List<GenreItem>
        @Suppress("UNCHECKED_CAST")
        val playlists = args[5] as List<Playlist>
        val targetSong = args[6] as SongUiModel?
        val isLoading = args[7] as Boolean

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
        FavoritesUiState(
            selectedTabIndex = tabIndex,
            favoriteSongs = songUiModels,
            favoriteAlbums = albums,
            favoriteArtists = artists,
            favoriteGenres = genres,
            playlists = playlists,
            targetSongForPlaylist = targetSong,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState(isLoading = true)
    )

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }

    fun onSongClicked(songId: String) {
        viewModelScope.launch {
            playerManager.playSongById(songId, uiState.value.favoriteSongs)
        }
    }

    fun playAllFavoriteSongs() {
        val songs = uiState.value.favoriteSongs
        if (songs.isNotEmpty()) {
            viewModelScope.launch {
                playerManager.setQueue(songs, startIndex = 0)
            }
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
                _toastChannel.send("Added to ${playlist.name}")
            } else {
                _toastChannel.send("Failed to add to playlist")
            }
            closeAddToPlaylistDialog()
        }
    }

    fun deleteSong(songId: Long) {
        viewModelScope.launch {
            playerManager.deleteSong(songId)
        }
    }
}
