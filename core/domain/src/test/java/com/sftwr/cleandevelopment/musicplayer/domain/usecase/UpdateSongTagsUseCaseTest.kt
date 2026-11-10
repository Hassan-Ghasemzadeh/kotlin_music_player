package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateSongTagsUseCaseTest {

    private class FakeMusicRepository : MusicRepository {
        var lastUpdatedSongId: Long? = null
        var lastTitle: String? = null
        var lastArtist: String? = null
        var lastAlbum: String? = null
        var lastYear: String? = null
        var lastGenre: String? = null
        var shouldReturnError = false

        override fun getDeviceSongs(): Flow<List<Song>> = emptyFlow()
        override fun getAlbums(): Flow<List<com.sftwr.cleandevelopment.musicplayer.domain.model.Album>> = emptyFlow()
        override fun getAlbumSongs(albumId: Long): Flow<List<Song>> = emptyFlow()
        override fun getArtists(): Flow<List<com.sftwr.cleandevelopment.musicplayer.domain.model.Artist>> = emptyFlow()
        override fun getArtistSongs(artistName: String): Flow<List<Song>> = emptyFlow()
        override fun getArtistAlbums(artistName: String): Flow<List<com.sftwr.cleandevelopment.musicplayer.domain.model.Album>> = emptyFlow()
        override fun getGenres(): Flow<List<com.sftwr.cleandevelopment.musicplayer.domain.model.Genre>> = emptyFlow()
        override fun getGenreSongs(genreName: String): Flow<List<Song>> = emptyFlow()
        override fun getFolders(): Flow<List<com.sftwr.cleandevelopment.musicplayer.domain.model.Folder>> = emptyFlow()
        override fun getFolderSongs(folderPath: String): Flow<List<Song>> = emptyFlow()

        override suspend fun syncWithDevice() {}

        override suspend fun updateSongTags(
            songId: Long,
            title: String,
            artist: String,
            album: String,
            year: String,
            genre: String
        ): UpdateTagResult {
            if (shouldReturnError) {
                return UpdateTagResult.Error("Write error")
            }
            lastUpdatedSongId = songId
            lastTitle = title
            lastArtist = artist
            lastAlbum = album
            lastYear = year
            lastGenre = genre
            return UpdateTagResult.Success
        }

        override suspend fun searchSongs(query: String): List<Song> = emptyList()

        override suspend fun deleteSong(songId: Long): DeleteResult = DeleteResult.Success
    }

    @Test
    fun invoke_delegatesParametersToRepository_returnsSuccess() {
        runBlocking {
            val fakeRepo = FakeMusicRepository()
            val useCase = UpdateSongTagsUseCase(fakeRepo)

            val result = useCase(
                songId = 42L,
                title = "New Title",
                artist = "New Artist",
                album = "New Album",
                year = "2024",
                genre = "Rock"
            )

            assertTrue(result is UpdateTagResult.Success)
            assertEquals(42L, fakeRepo.lastUpdatedSongId)
            assertEquals("New Title", fakeRepo.lastTitle)
            assertEquals("New Artist", fakeRepo.lastArtist)
            assertEquals("New Album", fakeRepo.lastAlbum)
            assertEquals("2024", fakeRepo.lastYear)
            assertEquals("Rock", fakeRepo.lastGenre)
        }
    }

    @Test
    fun invoke_whenRepositoryFails_returnsError() {
        runBlocking {
            val fakeRepo = FakeMusicRepository().apply { shouldReturnError = true }
            val useCase = UpdateSongTagsUseCase(fakeRepo)

            val result = useCase(
                songId = 42L,
                title = "Title",
                artist = "Artist",
                album = "Album",
                year = "2024",
                genre = "Pop"
            )

            assertTrue(result is UpdateTagResult.Error)
            assertEquals("Write error", (result as UpdateTagResult.Error).message)
        }
    }
}
