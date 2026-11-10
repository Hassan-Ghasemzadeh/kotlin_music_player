package com.sftwr.cleandevelopment.musicplayer.library.presentation.folders

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.FolderItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetFoldersUseCase
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
class FoldersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val foldersFlow = MutableStateFlow<List<FolderItem>>(emptyList())

    private val fakeRepository = object : MusicRepository {
        override fun getDeviceSongs(): Flow<List<Song>> = error("Not implemented")
        override fun getAlbums(): Flow<List<Album>> = error("Not implemented")
        override fun getAlbumSongs(albumId: Long): Flow<List<Song>> = error("Not implemented")
        override fun getArtists(): Flow<List<Artist>> = error("Not implemented")
        override fun getArtistSongs(artistName: String): Flow<List<Song>> = error("Not implemented")
        override fun getArtistAlbums(artistName: String): Flow<List<Album>> = error("Not implemented")
        override fun getGenres(): Flow<List<GenreItem>> = error("Not implemented")
        override fun getGenreSongs(genreName: String): Flow<List<Song>> = error("Not implemented")
        override fun getFolders(): Flow<List<FolderItem>> = foldersFlow
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

    private val getFoldersUseCase = GetFoldersUseCase(fakeRepository)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsFoldersFromUseCase() = runTest {
        val sampleFolders = listOf(
            FolderItem(path = "/storage/emulated/0/Music/Pop", name = "Pop", trackCount = 12),
            FolderItem(path = "/storage/emulated/0/Music/Rock", name = "Rock", trackCount = 8)
        )
        foldersFlow.value = sampleFolders

        val viewModel = FoldersViewModel(getFoldersUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.folders.size)
        assertEquals("Pop", state.folders[0].name)
    }

    @Test
    fun searchQuery_filtersFoldersCorrectly() = runTest {
        val sampleFolders = listOf(
            FolderItem(path = "/storage/emulated/0/Music/Pop", name = "Pop", trackCount = 12),
            FolderItem(path = "/storage/emulated/0/Music/Rock", name = "Rock", trackCount = 8)
        )
        foldersFlow.value = sampleFolders

        val viewModel = FoldersViewModel(getFoldersUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("Rock")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.folders.size)
        assertEquals("Rock", state.folders[0].name)
    }
}
