package com.sftwr.cleandevelopment.musicplayer.data

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongPreferencesRepositoryTest {

    private lateinit var repository: SongPreferencesRepository

    @Before
    fun setUp() {
        repository = FakeSongPreferencesRepository()
    }

    @Test
    fun saveLastPlayedSongId_storesIdSuccessfully() = runTest {
        repository.saveLastPlayedSongId("test_song_123")
        val result = repository.getLastPlayedSongId()
        assertEquals("test_song_123", result)
    }

    @Test
    fun clearLastPlayedSongId_removesStoredId() = runTest {
        repository.saveLastPlayedSongId("test_song_123")
        repository.clearLastPlayedSongId()
        val result = repository.getLastPlayedSongId()
        assertNull(result)
    }
}

private class FakeSongPreferencesRepository : SongPreferencesRepository {
    private var lastPlayedId: String? = null

    override suspend fun saveLastPlayedSongId(songId: String) {
        lastPlayedId = songId
    }

    override suspend fun getLastPlayedSongId(): String? {
        return lastPlayedId
    }

    override fun getLastPlayedSongIdFlow(): Flow<String?> {
        return flowOf(lastPlayedId)
    }

    override suspend fun clearLastPlayedSongId() {
        lastPlayedId = null
    }
}
