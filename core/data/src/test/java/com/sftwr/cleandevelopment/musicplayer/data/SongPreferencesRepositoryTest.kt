package com.sftwr.cleandevelopment.musicplayer.data

import com.sftwr.cleandevelopment.musicplayer.domain.model.LastPlayedSongState
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

    @Test
    fun saveLastPlayedSong_storesFullSongStateSuccessfully() = runTest {
        val songState = LastPlayedSongState(
            songId = "song_1",
            title = "Title 1",
            artist = "Artist 1",
            uri = "content://media/external/audio/media/1",
            duration = "03:45",
            durationInMs = 225000L,
            positionMs = 45000L,
            album = "Album 1"
        )
        repository.saveLastPlayedSong(songState)
        val result = repository.getLastPlayedSong()
        assertEquals(songState, result)
    }
}

private class FakeSongPreferencesRepository : SongPreferencesRepository {
    private var lastPlayedId: String? = null
    private var lastPlayedSongState: LastPlayedSongState? = null

    override suspend fun saveLastPlayedSongId(songId: String) {
        lastPlayedId = songId
    }

    override suspend fun getLastPlayedSongId(): String? {
        return lastPlayedId ?: lastPlayedSongState?.songId
    }

    override fun getLastPlayedSongIdFlow(): Flow<String?> {
        return flowOf(lastPlayedId ?: lastPlayedSongState?.songId)
    }

    override suspend fun saveLastPlayedSong(songState: LastPlayedSongState) {
        lastPlayedSongState = songState
        lastPlayedId = songState.songId
    }

    override suspend fun getLastPlayedSong(): LastPlayedSongState? {
        return lastPlayedSongState
    }

    override fun getLastPlayedSongFlow(): Flow<LastPlayedSongState?> {
        return flowOf(lastPlayedSongState)
    }

    override suspend fun clearLastPlayedSongId() {
        lastPlayedId = null
        lastPlayedSongState = null
    }
}
