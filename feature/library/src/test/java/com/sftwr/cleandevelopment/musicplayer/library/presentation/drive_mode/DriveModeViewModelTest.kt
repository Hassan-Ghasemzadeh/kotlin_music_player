package com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppSettings
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ToggleFavoriteUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.GetCurrentSongUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.GetIsPlayingUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.SkipNextUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.SkipPreviousUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.TogglePlayPauseUseCase
import com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode.viewmodel.DriveModeViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriveModeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakePlayerController = FakePlayerController()

    private var toggledFavoriteSongId: Long? = null

    private val togglePlayPauseUseCase = TogglePlayPauseUseCase(fakePlayerController)
    private val skipNextUseCase = SkipNextUseCase(fakePlayerController)
    private val skipPreviousUseCase = SkipPreviousUseCase(fakePlayerController)
    private val getCurrentSongUseCase = GetCurrentSongUseCase(fakePlayerController)
    private val getIsPlayingUseCase = GetIsPlayingUseCase(fakePlayerController)

    private val fakePlaylistRepository = object : PlaylistRepository {
        override fun getPlaylists(): Flow<List<Playlist>> = error("Not implemented")
        override fun getPlaylistSongs(playlistId: Long, playlistType: PlaylistType): Flow<List<Song>> = error("Not implemented")
        override fun getFavoriteAlbums(): Flow<List<Album>> = error("Not implemented")
        override fun getFavoriteArtists(): Flow<List<Artist>> = error("Not implemented")
        override fun getFavoriteGenres(): Flow<List<GenreItem>> = error("Not implemented")
        override suspend fun createCustomPlaylist(name: String, songIds: List<Long>): Long = 0
        override suspend fun deleteCustomPlaylist(playlistId: Long) {}
        override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {}
        override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {}
        override suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
            toggledFavoriteSongId = songId
        }
        override suspend fun toggleAlbumFavorite(albumId: Long, isFavorite: Boolean) {}
        override suspend fun toggleArtistFavorite(artistName: String, isFavorite: Boolean) {}
        override suspend fun toggleGenreFavorite(genreName: String, isFavorite: Boolean) {}
        override suspend fun recordSongPlayed(songId: Long) {}
    }

    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(fakePlaylistRepository)

    private val fakeSettingsRepository = object : SettingsRepository {
        override fun getSettings(): Flow<AppSettings> = MutableStateFlow(AppSettings())
        override suspend fun updateLanguage(language: AppLanguage) {}
        override suspend fun updateGlassmorphicStyle(enabled: Boolean) {}
        override suspend fun updateAlbumArtworkEnabled(enabled: Boolean) {}
        override suspend fun updateShowAlbumArt(enabled: Boolean) {}
        override suspend fun updatePlaybackSpeed(speed: Float) {}
        override suspend fun updatePlaybackPitch(pitch: Float) {}
        override suspend fun updateCrossfadeDuration(seconds: Int) {}
        override suspend fun updateSkipSilence(enabled: Boolean) {}
        override suspend fun updateDefaultEqualizerPreset(preset: EqualizerPreset) {}
        override suspend fun updateDefaultEqualizerPreset(preset: String) {}
        override suspend fun updateCircularVisualizerType(typeKey: String) {}
        override suspend fun updateReplayGainMode(mode: ReplayGainMode) {}
        override suspend fun addExcludedFolder(path: String) {}
        override suspend fun removeExcludedFolder(path: String) {}
        override suspend fun updateMinTrackDuration(seconds: Int) {}
        override suspend fun updateLockscreenControls(enabled: Boolean) {}
        override suspend fun updateScreenOffPlayback(enabled: Boolean) {}
        override suspend fun updateHeadsetControls(enabled: Boolean) {}
        override suspend fun updateAllowExternalDevicePlayback(enabled: Boolean) {}
        override suspend fun updateShakeToSkip(enabled: Boolean) {}
        override suspend fun updateSwipeArtworkToSkip(enabled: Boolean) {}
    }

    private val getSettingsUseCase = GetSettingsUseCase(fakeSettingsRepository)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsCorrectCurrentSongAndPlaybackState() = runTest {
        val song = SongUiModel(
            id = "101",
            title = "Highway Star",
            artist = "Deep Purple",
            album = "Machine Head",
            year = "1972",
            genre = "Rock",
            duration = "06:05",
            uri = "content://media/external/audio/media/101",
            isFavorite = true
        )
        fakePlayerController.currentSong.value = song
        fakePlayerController.isPlaying.value = true

        val viewModel = DriveModeViewModel(
            togglePlayPauseUseCase = togglePlayPauseUseCase,
            skipNextUseCase = skipNextUseCase,
            skipPreviousUseCase = skipPreviousUseCase,
            getCurrentSongUseCase = getCurrentSongUseCase,
            getIsPlayingUseCase = getIsPlayingUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getSettingsUseCase = getSettingsUseCase
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(song, state.currentSong)
        assertTrue(state.isPlaying)
        assertTrue(state.isFavorite)
    }

    @Test
    fun transportControls_delegateToUseCasesCorrectly() = runTest {
        val viewModel = DriveModeViewModel(
            togglePlayPauseUseCase = togglePlayPauseUseCase,
            skipNextUseCase = skipNextUseCase,
            skipPreviousUseCase = skipPreviousUseCase,
            getCurrentSongUseCase = getCurrentSongUseCase,
            getIsPlayingUseCase = getIsPlayingUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getSettingsUseCase = getSettingsUseCase
        )

        viewModel.togglePlayPause()
        assertTrue(fakePlayerController.playPauseToggled)

        viewModel.skipNext()
        assertTrue(fakePlayerController.skippedNext)

        viewModel.skipPrevious()
        assertTrue(fakePlayerController.skippedPrevious)
    }

    @Test
    fun toggleFavorite_invokesFavoriteUseCase() = runTest {
        val song = SongUiModel(
            id = "202",
            title = "Radar Love",
            artist = "Golden Earring",
            album = "Moontan",
            year = "1973",
            genre = "Rock",
            duration = "06:25",
            uri = "content://media/external/audio/media/202",
            isFavorite = false
        )
        fakePlayerController.currentSong.value = song

        val viewModel = DriveModeViewModel(
            togglePlayPauseUseCase = togglePlayPauseUseCase,
            skipNextUseCase = skipNextUseCase,
            skipPreviousUseCase = skipPreviousUseCase,
            getCurrentSongUseCase = getCurrentSongUseCase,
            getIsPlayingUseCase = getIsPlayingUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            getSettingsUseCase = getSettingsUseCase
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(202L, toggledFavoriteSongId)
    }

    private class FakePlayerController : PlayerController {
        override val currentSong = MutableStateFlow<SongUiModel?>(null)
        override val isPlaying = MutableStateFlow(false)
        var playPauseToggled = false
        var skippedNext = false
        var skippedPrevious = false

        override fun togglePlayPause() {
            playPauseToggled = true
        }

        override fun playNext() {
            skippedNext = true
        }

        override fun playPrevious() {
            skippedPrevious = true
        }
    }
}
