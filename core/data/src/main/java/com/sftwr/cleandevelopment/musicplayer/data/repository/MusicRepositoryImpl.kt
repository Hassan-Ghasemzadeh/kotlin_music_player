package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import com.sftwr.cleandevelopment.musicplayer.data.source.local.SongDao
import com.sftwr.cleandevelopment.musicplayer.data.util.AudioTagEditor
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioTagEditor: AudioTagEditor,
    private val songDao: SongDao // Inject local database(cache)
) : MusicRepository {

    /**
     * Returns a reactive Flow from the local database cache for instant UI loading,
     * while observing MediaStore changes to trigger background sync.
     */
    override fun getDeviceSongs(): Flow<List<Song>> {
        // Ui Source is local cache always
        return songDao.getAllSongsFlow()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Performs a background sync between device MediaStore and local Room database.
     * Call this on app start, resume, or pull-to-refresh.
     */
    override suspend fun syncWithDevice() = withContext(Dispatchers.IO) {
        try {
            val deviceSongs = fetchSongsFromMediaStore()
            val cachedSongs = songDao.getAllSongsOnce()

            val cachedDomainSongs = cachedSongs.map { it.toDomain() }
            val hasChanges = deviceSongs != cachedDomainSongs

            if (deviceSongs.isNotEmpty()) {
                if (hasChanges) {
                    songDao.upsertSongs(deviceSongs.map { it.toEntity() })
                }
                songDao.deleteStaleSongs(deviceSongs.map { it.path })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Scans MediaStore directly from device storage.
     */
    private fun fetchSongsFromMediaStore(): List<Song> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val songs = mutableListOf<Song>()
        context.contentResolver.query(
            collection,
            getProjection(),
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            songs.addAll(parseSongsFromCursor(cursor))
        }
        return songs
    }

    /**
     * Performs a filtered search on MediaStore based on title or artist query matching.
     */
    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val songs = mutableListOf<Song>()
        try {
            context.contentResolver.query(
                collection,
                getProjection(),
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                songs.addAll(parseSongsFromCursor(cursor))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        songs
    }

    private fun getProjection() = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATA
    )

    private fun parseSongsFromCursor(cursor: Cursor): List<Song> {
        val songs = mutableListOf<Song>()
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
        val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val title = cursor.getString(titleCol) ?: "Unknown"
            val artist = cursor.getString(artistCol) ?: "Unknown Artist"
            val album = cursor.getString(albumCol) ?: "Unknown Album"
            val albumId = cursor.getLong(albumIdCol)
            val duration = cursor.getLong(durationCol)
            val size = cursor.getLong(sizeCol)
            val path = cursor.getString(dataCol) ?: ""

            val contentUri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            val albumArtUri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                albumId
            ).toString()

            songs.add(
                Song(
                    id = id, title = title, artist = artist, album = album,
                    albumId = albumId, contentUri = contentUri, albumArtUri = albumArtUri,
                    duration = duration, size = size, path = path
                )
            )
        }
        return songs
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun deleteSong(songId: Long): DeleteResult = withContext(Dispatchers.IO) {
        try {
            val uri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            if (rowsDeleted > 0) {
                syncWithDevice() // همگام‌سازی پس از حذف
                DeleteResult.Success
            } else DeleteResult.Error
        } catch (e: RecoverableSecurityException) {
            DeleteResult.RequiresPermission(e.userAction.actionIntent.intentSender)
        } catch (_: SecurityException) {
            val uri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val pendingIntent =
                        MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
                    return@withContext DeleteResult.RequiresPermission(pendingIntent.intentSender)
                } catch (_: Exception) {
                    return@withContext DeleteResult.Error
                }
            }
            DeleteResult.Error
        } catch (e: Exception) {
            e.printStackTrace()
            DeleteResult.Error
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun updateSongTags(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        year: String,
        genre: String
    ): UpdateTagResult = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
        val tempFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.mp3")

        try {
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext UpdateTagResult.Error("Failed to read the audio file")

            audioTagEditor.updateTags(tempFile, title, artist, album, year, genre)

            contentResolver.openOutputStream(uri, "rwt")?.use { output ->
                tempFile.inputStream().use { input -> input.copyTo(output) }
            } ?: return@withContext UpdateTagResult.Error("Failed to write to the audio file")

            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, title)
                put(MediaStore.Audio.Media.ARTIST, artist)
                put(MediaStore.Audio.Media.ALBUM, album)
            }
            contentResolver.update(uri, contentValues, null, null)
            contentResolver.notifyChange(uri, null)

            val cachedSongs = songDao.getAllSongsOnce()
            val existingEntity = cachedSongs.find { it.id == songId }
            if (existingEntity != null) {
                val updatedEntity = existingEntity.copy(
                    title = title,
                    artist = artist,
                    album = album
                )
                songDao.upsertSongs(listOf(updatedEntity))
            }

            syncWithDevice()
            UpdateTagResult.Success

        } catch (e: RecoverableSecurityException) {
            UpdateTagResult.RequiresPermission(e.userAction.actionIntent.intentSender)
        } catch (e: SecurityException) {
            val uriTarget =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val pendingIntent =
                        MediaStore.createWriteRequest(context.contentResolver, listOf(uriTarget))
                    return@withContext UpdateTagResult.RequiresPermission(pendingIntent.intentSender)
                } catch (ex: Exception) {
                    return@withContext UpdateTagResult.Error(
                        ex.localizedMessage ?: "Permission error"
                    )
                }
            }
            UpdateTagResult.Error(e.localizedMessage ?: "Security access error")
        } catch (e: Exception) {
            UpdateTagResult.Error(e.localizedMessage ?: "An unknown error occurred")
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
}

// Extension Mappers for Clean Architecture Layer Isolation
private fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
    albumArtUri = "",
    duration = duration,
    size = size,
    path = path
)

private fun Song.toEntity() = SongEntity(
    id = id, title = title, artist = artist, album = album,
    albumId = albumId, duration = duration, size = size, path = path
)