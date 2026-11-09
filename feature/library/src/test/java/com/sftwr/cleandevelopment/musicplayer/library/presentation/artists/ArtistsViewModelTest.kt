package com.sftwr.cleandevelopment.musicplayer.library.presentation.artists

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetArtistsUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val artistsFlow = MutableStateFlow<List<Artist>>(emptyList())

    private val fakeRepository = object : MusicRepository {
        override fun getDeviceSongs(): Flow<List<Song>> = error("Not implemented")
        override fun getAlbums(): Flow<List<Album>> = error("Not implemented")
        override fun getAlbumSongs(albumId: Long): Flow<List<Song>> = error("Not implemented")
        override fun getArtists(): Flow<List<Artist>> = artistsFlow
        override fun getArtistSongs(artistName: String): Flow<List<Song>> = error("Not implemented")
        override fun getArtistAlbums(artistName: String): Flow<List<Album>> = error("Not implemented")
        override suspend fun searchSongs(query: String): List<Song> = emptyList()
        override suspend fun deleteSong(songId: Long): DeleteResult = DeleteResult.Success
        override suspend fun updateSongTags(
            songId: Long,
            title: String,
            artist: String,
            album: String,
            year: String,
            genre: String
        ): UpdateTagResult = UpdateTagResult.Success

        override suspend fun syncWithDevice() {}
    }

    private val getArtistsUseCase = GetArtistsUseCase(fakeRepository)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsArtistsFromUseCase() = runTest {
        val sampleArtists = listOf(
            Artist(name = "Adele", trackCount = 12, albumCount = 3),
            Artist(name = "Coldplay", trackCount = 20, albumCount = 4)
        )
        artistsFlow.value = sampleArtists

        val viewModel = ArtistsViewModel(getArtistsUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.artists.size)
        assertEquals("Adele", state.artists[0].name)
    }

    @Test
    fun searchQuery_filtersArtistListCorrectly() = runTest {
        val sampleArtists = listOf(
            Artist(name = "Adele", trackCount = 12, albumCount = 3),
            Artist(name = "Coldplay", trackCount = 20, albumCount = 4)
        )
        artistsFlow.value = sampleArtists

        val viewModel = ArtistsViewModel(getArtistsUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Cold")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.artists.size)
        assertEquals("Coldplay", state.artists[0].name)
    }
}
