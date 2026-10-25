package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.ContentObserver
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import androidx.core.net.toUri
import com.sftwr.cleandevelopment.musicplayer.data.util.AudioTagEditor
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioTagEditor: AudioTagEditor,
) : MusicRepository {

    /**
     * Observes device audio files from MediaStore and emits an updated list reactively using a Flow.
     */
    override fun getDeviceSongs(): Flow<List<Song>> = callbackFlow {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        // Helper function to query MediaStore and emit the song list
        fun fetchSongs() {
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
            trySend(songs)
        }

        // Emit initial set of songs
        fetchSongs()

        // Register a ContentObserver to listen for external database changes (e.g., new downloads or deletions)
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                fetchSongs()
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        // Clean up the observer when the Flow collector cancels or completes
        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }.flowOn(Dispatchers.IO)

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

    /**
     * Defines the required columns projection for querying audio files from MediaStore.
     */
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

    /**
     * Parses a database Cursor and maps it into a list of domain [Song] models.
     */
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

    /**
     * Deletes a specific audio file from device storage with proper Scoped Storage permission handling.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun deleteSong(songId: Long): DeleteResult = withContext(Dispatchers.IO) {
        try {
            val uri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            if (rowsDeleted > 0) DeleteResult.Success else DeleteResult.Error
        } catch (e: RecoverableSecurityException) {
            // Handled for Android 10 (API 29) file deletion permission consent
            DeleteResult.RequiresPermission(e.userAction.actionIntent.intentSender)
        } catch (_: SecurityException) {
            val uri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    // Handled for Android 11+ (API 30+) batch delete request API
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

    /**
     * Updates track metadata tags safely by copying the file to cache, editing tags via AudioTagEditor,
     * writing back to the content stream, and notifying MediaStore.
     */
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
            // Step 1: Copy media stream to a temporary local file for safe tag manipulation
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext UpdateTagResult.Error("Failed to read the audio file")

            // Step 2: Edit audio tags using external modular editor helper
            audioTagEditor.updateTags(tempFile, title, artist, album, year, genre)

            // Step 3: Write modified temporary file back to content resolver output stream
            contentResolver.openOutputStream(uri, "w")?.use { output ->
                tempFile.inputStream().use { input -> input.copyTo(output) }
            } ?: return@withContext UpdateTagResult.Error("Failed to write to the audio file")

            // Step 4: Update MediaStore database records to reflect changes immediately
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, title)
                put(MediaStore.Audio.Media.ARTIST, artist)
                put(MediaStore.Audio.Media.ALBUM, album)
            }
            contentResolver.update(uri, contentValues, null, null)
            contentResolver.notifyChange(uri, null)

            UpdateTagResult.Success

        } catch (e: RecoverableSecurityException) {
            // Handle Android 10 scoped storage write permission request
            UpdateTagResult.RequiresPermission(e.userAction.actionIntent.intentSender)
        } catch (e: SecurityException) {
            val uriTarget =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    // Handle Android 11+ explicit write request intent
                    val pendingIntent =
                        MediaStore.createWriteRequest(context.contentResolver, listOf(uriTarget))
                    return@withContext UpdateTagResult.RequiresPermission(pendingIntent.intentSender)
                } catch (ex: Exception) {
                    return@withContext UpdateTagResult.Error(
                        ex.localizedMessage ?: "Permission request error"
                    )
                }
            }
            UpdateTagResult.Error(e.localizedMessage ?: "Security access error")
        } catch (e: Exception) {
            UpdateTagResult.Error(e.localizedMessage ?: "An unknown error occurred")
        } finally {
            // Ensure temporary file cleanup to prevent cache bloat
            if (tempFile.exists()) tempFile.delete()
        }
    }
}