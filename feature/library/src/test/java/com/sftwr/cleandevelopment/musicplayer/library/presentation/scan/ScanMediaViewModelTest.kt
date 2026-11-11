package com.sftwr.cleandevelopment.musicplayer.library.presentation.scan

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.FolderItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.ScanProgress
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MediaScanRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetScanningProgressUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ScanMediaUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanMediaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeScanRepository: FakeMediaScanRepositoryForViewModel
    private lateinit var fakeMusicRepository: FakeMusicRepositoryForViewModel
    private lateinit var scanMediaUseCase: ScanMediaUseCase
    private lateinit var getScanningProgressUseCase: GetScanningProgressUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeScanRepository = FakeMediaScanRepositoryForViewModel()
        fakeMusicRepository = FakeMusicRepositoryForViewModel()
        scanMediaUseCase = ScanMediaUseCase(fakeScanRepository)
        getScanningProgressUseCase = GetScanningProgressUseCase(fakeScanRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initialization_startsScanAndUpdatesUiState() = runTest {
        val viewModel = ScanMediaViewModel(
            scanMediaUseCase = scanMediaUseCase,
            getScanningProgressUseCase = getScanningProgressUseCase,
            musicRepository = fakeMusicRepository,
            mediaScanRepository = fakeScanRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isScanning)
        assertEquals(25, viewModel.uiState.value.scannedCount)
    }

    @Test
    fun onOkClick_syncsDeviceAndResetsState() = runTest {
        val viewModel = ScanMediaViewModel(
            scanMediaUseCase = scanMediaUseCase,
            getScanningProgressUseCase = getScanningProgressUseCase,
            musicRepository = fakeMusicRepository,
            mediaScanRepository = fakeScanRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        var dismissed = false
        viewModel.onOkClick { dismissed = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(dismissed)
        assertTrue(fakeMusicRepository.synced)
        assertFalse(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun dismiss_resetsStateAndInvokesCallback() = runTest {
        val viewModel = ScanMediaViewModel(
            scanMediaUseCase = scanMediaUseCase,
            getScanningProgressUseCase = getScanningProgressUseCase,
            musicRepository = fakeMusicRepository,
            mediaScanRepository = fakeScanRepository
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        var dismissed = false
        viewModel.dismiss { dismissed = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(dismissed)
        assertFalse(viewModel.uiState.value.isCompleted)
    }
}

class FakeMediaScanRepositoryForViewModel : MediaScanRepository {
    private val _progress = MutableStateFlow(ScanProgress())

    override fun getScanningProgress(): Flow<ScanProgress> = _progress

    override suspend fun startScan() {
        _progress.value = ScanProgress(isScanning = true, currentPath = "/storage/emulated/0/Music", scannedCount = 10)
        _progress.value = ScanProgress(isScanning = false, currentPath = "", scannedCount = 25, isCompleted = true)
    }

    override fun resetScanState() {
        _progress.value = ScanProgress()
    }
}

class FakeMusicRepositoryForViewModel : MusicRepository {
    var synced = false

    override fun getDeviceSongs(): Flow<List<Song>> = flowOf(emptyList())
    override fun getAlbums(): Flow<List<Album>> = flowOf(emptyList())
    override fun getAlbumSongs(albumId: Long): Flow<List<Song>> = flowOf(emptyList())
    override fun getArtists(): Flow<List<Artist>> = flowOf(emptyList())
    override fun getArtistSongs(artistName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getArtistAlbums(artistName: String): Flow<List<Album>> = flowOf(emptyList())
    override fun getGenres(): Flow<List<GenreItem>> = flowOf(emptyList())
    override fun getGenreSongs(genreName: String): Flow<List<Song>> = flowOf(emptyList())
    override fun getFolders(): Flow<List<FolderItem>> = flowOf(emptyList())
    override fun getFolderSongs(folderPath: String): Flow<List<Song>> = flowOf(emptyList())
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

    override suspend fun syncWithDevice() {
        synced = true
    }
}
