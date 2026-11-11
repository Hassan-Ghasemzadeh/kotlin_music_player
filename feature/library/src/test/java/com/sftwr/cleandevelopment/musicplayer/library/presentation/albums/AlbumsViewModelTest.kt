package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetAlbumsUseCase
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
class AlbumsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())

    private val fakeRepository = object : MusicRepository {
        override fun getDeviceSongs(): Flow<List<Song>> = error("Not implemented")
        override fun getAlbums(): Flow<List<Album>> = albumsFlow
        override fun getAlbumSongs(albumId: Long): Flow<List<Song>> = error("Not implemented")
        override fun getArtists(): Flow<List<Artist>> = error("Not implemented")
        override fun getArtistSongs(artistName: String): Flow<List<Song>> = error("Not implemented")
        override fun getArtistAlbums(artistName: String): Flow<List<Album>> = error("Not implemented")
        override fun getGenres(): Flow<List<com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem>> = error("Not implemented")
        override fun getGenreSongs(genreName: String): Flow<List<Song>> = error("Not implemented")
        override fun getFolders(): Flow<List<com.sftwr.cleandevelopment.musicplayer.domain.model.FolderItem>> = error("Not implemented")
        override fun getFolderSongs(folderPath: String): Flow<List<Song>> = error("Not implemented")
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

    private val getAlbumsUseCase = GetAlbumsUseCase(fakeRepository)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsAlbumsFromUseCase() = runTest {
        val sampleAlbums = listOf(
            Album(albumId = 1L, title = "Album One", artist = "Artist A", trackCount = 10),
            Album(albumId = 2L, title = "Album Two", artist = "Artist B", trackCount = 5)
        )
        albumsFlow.value = sampleAlbums

        val viewModel = AlbumsViewModel(getAlbumsUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.albums.size)
        assertEquals("Album One", state.albums[0].title)
    }

    @Test
    fun searchQuery_filtersAlbumListCorrectly() = runTest {
        val sampleAlbums = listOf(
            Album(albumId = 1L, title = "Midnight Memories", artist = "One Direction", trackCount = 14),
            Album(albumId = 2L, title = "Night Visions", artist = "Imagine Dragons", trackCount = 11)
        )
        albumsFlow.value = sampleAlbums

        val viewModel = AlbumsViewModel(getAlbumsUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Midnight")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.albums.size)
        assertEquals("Midnight Memories", state.albums[0].title)
    }
}
