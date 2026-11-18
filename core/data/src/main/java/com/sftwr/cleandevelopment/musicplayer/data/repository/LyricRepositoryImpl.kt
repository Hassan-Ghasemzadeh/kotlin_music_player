package com.sftwr.cleandevelopment.musicplayer.data.repository

import com.sftwr.cleandevelopment.musicplayer.data.model.LyricEntity
import com.sftwr.cleandevelopment.musicplayer.data.source.local.LyricDao
import com.sftwr.cleandevelopment.musicplayer.data.source.remote.OnlineLyricFetcher
import com.sftwr.cleandevelopment.musicplayer.data.util.LrcParser
import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricRepositoryImpl @Inject constructor(
    private val lyricDao: LyricDao
) : LyricRepository {

    override fun getLyricForSong(songId: Long): Flow<Lyric?> {
        return lyricDao.getLyricBySongId(songId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun getLyricForSongDirect(songId: Long): Lyric? {
        return lyricDao.getLyricBySongIdDirect(songId)?.toDomainModel()
    }

    override fun getAllLyrics(): Flow<List<Lyric>> {
        return lyricDao.getAllLyrics().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun saveLyric(lyric: Lyric) {
        val parsed = LrcParser.parse(lyric.content)
        val entity = LyricEntity(
            songId = lyric.songId,
            title = lyric.title,
            artist = lyric.artist,
            content = lyric.content,
            isSynced = parsed.isSynced,
            lastModified = System.currentTimeMillis()
        )
        lyricDao.insertLyric(entity)
    }

    override suspend fun deleteLyric(songId: Long) {
        lyricDao.deleteLyricBySongId(songId)
    }

    override suspend fun renameLyric(songId: Long, newTitle: String, newArtist: String) {
        lyricDao.renameLyric(songId, newTitle, newArtist)
    }

    override suspend fun searchLyricOnline(title: String, artist: String): Result<Lyric> {
        val fetchResult = OnlineLyricFetcher.fetchLyrics(title, artist)
        return fetchResult.map { rawContent ->
            val parsed = LrcParser.parse(rawContent)
            Lyric(
                songId = -1L, // Placeholder, will be bound when saved for specific song
                title = title,
                artist = artist,
                content = rawContent,
                isSynced = parsed.isSynced,
                lines = parsed.lines,
                lastModified = System.currentTimeMillis()
            )
        }
    }

    private fun LyricEntity.toDomainModel(): Lyric {
        val parsed = LrcParser.parse(content)
        return Lyric(
            songId = songId,
            title = title,
            artist = artist,
            content = content,
            isSynced = parsed.isSynced,
            lines = parsed.lines,
            lastModified = lastModified
        )
    }
}
