package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import kotlinx.coroutines.flow.Flow

interface LyricRepository {
    fun getLyricForSong(songId: Long): Flow<Lyric?>
    suspend fun getLyricForSongDirect(songId: Long): Lyric?
    fun getAllLyrics(): Flow<List<Lyric>>
    suspend fun saveLyric(lyric: Lyric)
    suspend fun deleteLyric(songId: Long)
    suspend fun renameLyric(songId: Long, newTitle: String, newArtist: String)
    suspend fun searchLyricOnline(title: String, artist: String): Result<Lyric>
}
