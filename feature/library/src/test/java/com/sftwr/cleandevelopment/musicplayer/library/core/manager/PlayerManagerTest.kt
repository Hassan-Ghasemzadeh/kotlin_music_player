package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePreferencesRepository: FakeSongPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePreferencesRepository = FakeSongPreferencesRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fake preferences saves and restores song id correctly`() = runTest(testDispatcher) {
        fakePreferencesRepository.saveLastPlayedSongId("101")
        val savedId = fakePreferencesRepository.getLastPlayedSongId()
        assertEquals("101", savedId)
    }

    @Test
    fun `fake preferences clears song id correctly`() = runTest(testDispatcher) {
        fakePreferencesRepository.saveLastPlayedSongId("101")
        fakePreferencesRepository.clearLastPlayedSongId()
        val savedId = fakePreferencesRepository.getLastPlayedSongId()
        assertNull(savedId)
    }
}

class FakeSongPreferencesRepository : SongPreferencesRepository {
    var storedSongId: String? = null

    override suspend fun saveLastPlayedSongId(songId: String) {
        storedSongId = songId
    }

    override suspend fun getLastPlayedSongId(): String? {
        return storedSongId
    }

    override fun getLastPlayedSongIdFlow(): Flow<String?> = flowOf(storedSongId)

    override suspend fun clearLastPlayedSongId() {
        storedSongId = null
    }
}
