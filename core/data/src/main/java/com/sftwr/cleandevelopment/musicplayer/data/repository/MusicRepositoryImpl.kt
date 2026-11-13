package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.sftwr.cleandevelopment.musicplayer.data.model.AlbumQueryResult
import com.sftwr.cleandevelopment.musicplayer.data.model.ArtistQueryResult
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import com.sftwr.cleandevelopment.musicplayer.data.source.local.SongDao
import com.sftwr.cleandevelopment.musicplayer.data.util.AudioTagEditor
import com.sftwr.cleandevelopment.musicplayer.data.util.FolderFilterHelper
import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.Folder
import com.sftwr.cleandevelopment.musicplayer.domain.model.FolderItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Genre
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

class MusicRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val audioTagEditor: AudioTagEditor,
    private val songDao: SongDao, // Inject local database(cache)
    private val settingsRepository: SettingsRepository
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

    override fun getAlbums(): Flow<List<Album>> {
        return songDao.getAlbumsFlow()
            .map { results ->
                results.map { result ->
                    Album(
                        albumId = result.albumId,
                        title = result.title.ifBlank { "Unknown Album" },
                        artist = result.artist.ifBlank { "Unknown Artist" },
                        trackCount = result.trackCount,
                        albumArtUri = "content://media/external/audio/albumart/${result.albumId}",
                        isFavorite = result.isFavorite
                    )
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getAlbumSongs(albumId: Long): Flow<List<Song>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.filter { it.albumId == albumId }
                    .map { it.toDomain() }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getArtists(): Flow<List<Artist>> {
        return songDao.getArtistsFlow()
            .map { results ->
                results.map { result ->
                    Artist(
                        name = result.name.ifBlank { "Unknown Artist" },
                        trackCount = result.trackCount,
                        albumCount = result.albumCount,
                        artistArtUri = "content://media/external/audio/albumart/${result.albumId}",
                        isFavorite = result.isFavorite
                    )
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getArtistSongs(artistName: String): Flow<List<Song>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.filter { it.artist.ifBlank { "Unknown Artist" } == artistName }
                    .map { it.toDomain() }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getArtistAlbums(artistName: String): Flow<List<Album>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.filter { it.artist.ifBlank { "Unknown Artist" } == artistName }
                    .groupBy { it.albumId }
                    .map { (albumId, songEntities) ->
                        val first = songEntities.first()
                        val isFav = songEntities.isNotEmpty() && songEntities.all { it.isFavorite }
                        Album(
                            albumId = albumId,
                            title = first.album.ifBlank { "Unknown Album" },
                            artist = first.artist.ifBlank { "Unknown Artist" },
                            trackCount = songEntities.size,
                            albumArtUri = "content://media/external/audio/albumart/$albumId",
                            isFavorite = isFav
                        )
                    }
                    .sortedBy { it.title }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getGenres(): Flow<List<GenreItem>> {
        return songDao.getGenresWithCountFlow()
            .map { results ->
                results.map { result ->
                    Genre(
                        name = result.genre.ifBlank { "Unknown Genre" },
                        trackCount = result.trackCount
                    )
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getGenreSongs(genreName: String): Flow<List<Song>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.filter { it.genre.ifBlank { "Unknown Genre" } == genreName }
                    .map { it.toDomain() }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getFolders(): Flow<List<FolderItem>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.groupBy { getParentFolderPath(it.path) }
                    .filterKeys { it.isNotEmpty() }
                    .map { (folderPath, songEntities) ->
                        Folder(
                            path = folderPath,
                            name = getFolderName(folderPath),
                            trackCount = songEntities.size
                        )
                    }
                    .sortedBy { it.name }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getFolderSongs(folderPath: String): Flow<List<Song>> {
        return songDao.getAllSongsFlow()
            .map { entities ->
                entities.filter { getParentFolderPath(it.path) == folderPath }
                    .map { it.toDomain() }
            }
            .flowOn(Dispatchers.IO)
    }

    private fun getParentFolderPath(path: String): String {
        if (path.isBlank()) return ""
        val file = File(path)
        return file.parent ?: ""
    }

    private fun getFolderName(folderPath: String): String {
        if (folderPath.isBlank()) return "Unknown Folder"
        val file = File(folderPath)
        return file.name.ifBlank { "Unknown Folder" }
    }

    /**
     * Performs a background sync between device MediaStore and local Room database.
     * Call this on app start, resume, or pull-to-refresh.
     */
    /**
     * Performs a background sync between device MediaStore and local Room database.
     * Extracts year and genre once during device scan/sync and saves them directly into SongEntity.
     */
    override suspend fun syncWithDevice() = withContext(Dispatchers.IO) {
        try {
            val deviceEntities = fetchSongsFromMediaStore()
            val cachedSongs = songDao.getAllSongsOnce()
            val cachedMap = cachedSongs.associateBy { it.id }

            val mergedEntities = deviceEntities.map { deviceEntity ->
                val cached = cachedMap[deviceEntity.id]
                if (cached != null) {
                    deviceEntity.copy(
                        isFavorite = cached.isFavorite,
                        playCount = cached.playCount,
                        lastPlayedTimestamp = cached.lastPlayedTimestamp,
                        dateAdded = if (cached.dateAdded > 0) cached.dateAdded else deviceEntity.dateAdded
                    )
                } else {
                    deviceEntity
                }
            }

            val cachedDomainSongs = cachedSongs.map { it.toDomain() }
            val deviceDomainSongs = mergedEntities.map { it.toDomain() }
            val hasChanges = deviceDomainSongs != cachedDomainSongs

            if (mergedEntities.isNotEmpty()) {
                if (hasChanges) {
                    songDao.upsertSongs(mergedEntities)
                }
                songDao.deleteStaleSongs(mergedEntities.map { it.path })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Scans MediaStore directly from device storage and populates SongEntity with metadata.
     */
    private suspend fun fetchSongsFromMediaStore(): List<SongEntity> {
        val settings = runCatching { settingsRepository.getSettings().first() }.getOrNull()
        val excludedFolders = settings?.excludedFolders ?: emptySet()
        val minTrackDurationMs = (settings?.minTrackDurationSeconds ?: 30) * 1000L

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val songEntities = mutableListOf<SongEntity>()
        context.contentResolver.query(
            collection,
            getProjection(),
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            songEntities.addAll(parseSongEntitiesFromCursor(cursor, excludedFolders, minTrackDurationMs))
        }
        return songEntities
    }

    /**
     * Performs a filtered search on MediaStore based on title or artist query matching.
     */
    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val settings = runCatching { settingsRepository.getSettings().first() }.getOrNull()
        val excludedFolders = settings?.excludedFolders ?: emptySet()
        val minTrackDurationMs = (settings?.minTrackDurationSeconds ?: 30) * 1000L

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val songEntities = mutableListOf<SongEntity>()
        try {
            context.contentResolver.query(
                collection,
                getProjection(),
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                songEntities.addAll(parseSongEntitiesFromCursor(cursor, excludedFolders, minTrackDurationMs))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        songEntities.map { it.toDomain() }
    }

    private fun getProjection() = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DATE_ADDED
    )

    private fun parseSongEntitiesFromCursor(
        cursor: Cursor,
        excludedFolders: Set<String>,
        minTrackDurationMs: Long
    ): List<SongEntity> {
        val songEntities = mutableListOf<SongEntity>()
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
        val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val title = cursor.getString(titleCol) ?: "Unknown"
            val artist = cursor.getString(artistCol) ?: "Unknown Artist"
            val album = cursor.getString(albumCol) ?: "Unknown Album"
            val albumId = cursor.getLong(albumIdCol)
            val duration = cursor.getLong(durationCol)
            val size = cursor.getLong(sizeCol)
            val path = cursor.getString(dataCol) ?: ""

            val parentFolder = if (path.isNotEmpty()) {
                File(path).parent ?: path
            } else "Storage"

            val isExcluded = FolderFilterHelper.isPathExcluded(path, excludedFolders, context)
            val isTooShort = duration < minTrackDurationMs

            if (isExcluded || isTooShort) {
                continue
            }

            val rawDateAdded = if (dateAddedCol != -1) cursor.getLong(dateAddedCol) else 0L
            val dateAdded = if (rawDateAdded > 0) rawDateAdded * 1000L else System.currentTimeMillis()

            // Extract extra metadata (year and genre) once during device scan/sync
            val (year, genre) = extractMetadataFromFile(path)

            songEntities.add(
                SongEntity(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    albumId = albumId,
                    duration = duration,
                    size = size,
                    path = path,
                    year = year,
                    genre = genre,
                    dateAdded = dateAdded
                )
            )
        }
        return songEntities
    }

    private fun extractMetadataFromFile(path: String): Pair<String, String> {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) ?: ""
            Pair(year, genre)
        } catch (_: Exception) {
            Pair("", "")
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
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
                    album = album,
                    year = year,
                    genre = genre
                )
                // Immediately refresh the local Room database/cache for this specific song
                songDao.upsertSongs(listOf(updatedEntity))

                // Targeted scan specifically for the modified song file instead of full device rescan
                if (existingEntity.path.isNotEmpty()) {
                    suspendCancellableCoroutine { continuation ->
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(existingEntity.path),
                            arrayOf("audio/*")
                        ) { _, _ ->
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }
                }
            }

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
    albumArtUri = null,
    duration = duration,
    size = size,
    path = path,
    year = year,
    genre = genre,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayedTimestamp = lastPlayedTimestamp,
    dateAdded = dateAdded
)

private fun Song.toEntity() = SongEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    duration = duration,
    size = size,
    path = path,
    year = year,
    genre = genre,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayedTimestamp = lastPlayedTimestamp,
    dateAdded = dateAdded
)