package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.ScanProgress
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MediaScanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanMediaUseCaseTest {

    private lateinit var fakeRepository: FakeMediaScanRepository
    private lateinit var scanMediaUseCase: ScanMediaUseCase
    private lateinit var getScanningProgressUseCase: GetScanningProgressUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeMediaScanRepository()
        scanMediaUseCase = ScanMediaUseCase(fakeRepository)
        getScanningProgressUseCase = GetScanningProgressUseCase(fakeRepository)
    }

    @Test
    fun getScanningProgress_returnsInitialIdleState() = runTest {
        val initialProgress = getScanningProgressUseCase().first()
        assertFalse(initialProgress.isScanning)
        assertFalse(initialProgress.isCompleted)
        assertEquals(0, initialProgress.scannedCount)
    }

    @Test
    fun scanMediaUseCase_triggersStartScanInRepository() = runTest {
        scanMediaUseCase()
        val progress = getScanningProgressUseCase().first()
        assertTrue(progress.isCompleted)
        assertFalse(progress.isScanning)
        assertEquals(15, progress.scannedCount)
    }
}

private class FakeMediaScanRepository : MediaScanRepository {
    private val _progress = MutableStateFlow(ScanProgress())

    override fun getScanningProgress(): Flow<ScanProgress> = _progress

    override suspend fun startScan() {
        _progress.value = ScanProgress(isScanning = true, currentPath = "/storage/emulated/0/Music", scannedCount = 5)
        _progress.value = ScanProgress(isScanning = false, currentPath = "", scannedCount = 15, isCompleted = true)
    }

    override fun resetScanState() {
        _progress.value = ScanProgress()
    }
}
