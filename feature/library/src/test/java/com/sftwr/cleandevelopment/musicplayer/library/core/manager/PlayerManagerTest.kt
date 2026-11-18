package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import com.sftwr.cleandevelopment.musicplayer.domain.model.LastPlayedSongState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
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
    var storedSongState: LastPlayedSongState? = null

    override suspend fun saveLastPlayedSongId(songId: String) {
        storedSongId = songId
    }

    override suspend fun getLastPlayedSongId(): String? {
        return storedSongId ?: storedSongState?.songId
    }

    override fun getLastPlayedSongIdFlow(): Flow<String?> = flowOf(storedSongId ?: storedSongState?.songId)

    override suspend fun saveLastPlayedSong(songState: LastPlayedSongState) {
        storedSongState = songState
        storedSongId = songState.songId
    }

    override suspend fun getLastPlayedSong(): LastPlayedSongState? {
        return storedSongState
    }

    override fun getLastPlayedSongFlow(): Flow<LastPlayedSongState?> = flowOf(storedSongState)

    override suspend fun clearLastPlayedSongId() {
        storedSongId = null
        storedSongState = null
    }
}
