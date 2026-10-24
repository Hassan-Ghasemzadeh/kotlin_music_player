package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
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
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val application: Application,
) : MusicRepository {

    /**
     * Observes device audio files from MediaStore and emits an updated list of songs reactively.
     */
    override fun getDeviceSongs(): Flow<List<Song>> = callbackFlow {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        // Filter out actual music files (excluding short recordings, ringtones, or notifications)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        fun fetchSongs() {
            songs.clear()
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val path = cursor.getString(dataColumn) ?: ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        albumId
                    ).toString()

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            duration = duration,
                            size = size,
                            path = path
                        )
                    )
                }
            }
            trySend(songs)
        }

        // Initial loading of songs
        fetchSongs()

        // Register a ContentObserver to listen for MediaStore changes (e.g. downloaded new songs)
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                fetchSongs() // Re-query songs whenever media store content changes or new files are added
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        // Unregister the observer when the flow is closed
        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun searchSongs(query: String): List<Song> {
        if (query.isBlank()) return emptyList()

        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val path = cursor.getString(dataColumn) ?: ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        albumId
                    ).toString()

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            duration = duration,
                            size = size,
                            path = path
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return songs
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun deleteSong(songId: Long): DeleteResult {
        return withContext(Dispatchers.IO) {
            try {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songId
                )

                val rowsDeleted = context.contentResolver.delete(uri, null, null)
                android.util.Log.d("DeleteDebug", "Target ID: $songId, Rows Deleted: $rowsDeleted")
                if (rowsDeleted > 0) DeleteResult.Success else DeleteResult.Error
            } catch (e: android.app.RecoverableSecurityException) {
                DeleteResult.RequiresPermission(e.userAction.actionIntent.intentSender)
            } catch (e: SecurityException) {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songId
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        val pendingIntent = MediaStore.createDeleteRequest(
                            application.contentResolver,
                            listOf(uri)
                        )
                        return@withContext DeleteResult.RequiresPermission(pendingIntent.intentSender)
                    } catch (_: Exception) {
                        return@withContext DeleteResult.Error
                    }
                }
                e.printStackTrace()
                DeleteResult.Error
            } catch (e: Exception) {
                e.printStackTrace()
                DeleteResult.Error
            }
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
        val contentResolver = application.contentResolver
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songId
        )

        val tempFile = File(application.cacheDir, "temp_audio_$songId.mp3")

        try {
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext UpdateTagResult.Error("امکان خواندن فایل وجود ندارد")

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val audioFile = AudioFileIO.read(tempFile)

            // 1. ذخیره آرت‌ورک‌های موجود قبل از پاکسازی تگ‌ها
            val existingArtworks = audioFile.tag?.artworkList?.toList() ?: emptyList()

            // 2. پاکسازی تگ‌های معیوب یا غیراستانداردِ قبلی (مشابه کاری که سامسونگ موزیک انجام می‌دهد)
            try {
                audioFile.setTag(null)
                val tag = audioFile.createDefaultTag()
                audioFile.tag = tag
            } catch (_: Exception) {
                // اگر فایل تگ نداشت یا حذف نشد، نادیده بگیر و ادامه بده
            }

            // 3. ساخت تگ جدید و استاندارد
            val tag = audioFile.createDefaultTag()
            audioFile.tag = tag

            tag.setField(FieldKey.TITLE, title)
            tag.setField(FieldKey.ARTIST, artist)
            tag.setField(FieldKey.ALBUM, album)

            if (year.isNotBlank() && year != "نامشخص") {
                tag.setField(FieldKey.YEAR, year)
            }
            if (genre.isNotBlank() && genre != "نامشخص") {
                tag.setField(FieldKey.GENRE, genre)
            }

            // 4. بازگرداندن تصاویر کاور روی تگ جدید
            for (artwork in existingArtworks) {
                tag.addField(artwork)
            }

            audioFile.commit()

            val outputStream = contentResolver.openOutputStream(uri, "w")
                ?: return@withContext UpdateTagResult.Error("امکان نوشتن روی فایل وجود ندارد")

            outputStream.use { output ->
                tempFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, title)
                put(MediaStore.Audio.Media.ARTIST, artist)
                put(MediaStore.Audio.Media.ALBUM, album)
            }
            contentResolver.update(uri, contentValues, null, null)
            contentResolver.notifyChange(uri, null)

            tempFile.delete()
            UpdateTagResult.Success

        } catch (e: RecoverableSecurityException) {
            tempFile.delete()
            val intentSender = e.userAction.actionIntent.intentSender
            android.util.Log.e(
                "EditTagError",
                "Failed to update tags for song: ${e.localizedMessage}",
                e
            )
            UpdateTagResult.RequiresPermission(intentSender)

        } catch (e: SecurityException) {
            tempFile.delete()
            val uriTarget =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val pendingIntent = MediaStore.createWriteRequest(
                        application.contentResolver,
                        listOf(uriTarget)
                    )
                    return@withContext UpdateTagResult.RequiresPermission(pendingIntent.intentSender)
                } catch (ex: Exception) {
                    return@withContext UpdateTagResult.Error(
                        ex.localizedMessage ?: "خطا در درخواست مجوز"
                    )
                }
            }
            android.util.Log.e(
                "EditTagError",
                "Failed to update tags for song: ${e.localizedMessage}",
                e
            )
            UpdateTagResult.Error(e.localizedMessage ?: "خطای دسترسی امنیتی")
        } catch (e: Exception) {
            tempFile.delete()
            android.util.Log.e(
                "EditTagError",
                "Failed to update tags for song: ${e.localizedMessage}",
                e
            )
            UpdateTagResult.Error(e.localizedMessage ?: "خطای ناشناخته رخ داد")
        }
    }
}