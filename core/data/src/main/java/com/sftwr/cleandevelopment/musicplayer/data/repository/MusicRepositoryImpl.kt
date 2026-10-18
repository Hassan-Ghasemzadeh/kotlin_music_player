package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.provider.MediaStore
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

class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
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
}