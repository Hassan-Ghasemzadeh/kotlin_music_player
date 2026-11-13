package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import com.sftwr.cleandevelopment.musicplayer.data.source.local.SongDao
import com.sftwr.cleandevelopment.musicplayer.data.util.FolderFilterHelper
import com.sftwr.cleandevelopment.musicplayer.domain.model.ScanProgress
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MediaScanRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val settingsRepository: SettingsRepository
) : MediaScanRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _scanProgress = MutableStateFlow(ScanProgress())
    private var scanJob: Job? = null

    override fun getScanningProgress(): Flow<ScanProgress> = _scanProgress.asStateFlow()

    override suspend fun startScan() = withContext(Dispatchers.IO) {
        scanJob?.cancel()

        _scanProgress.update {
            ScanProgress(
                isScanning = true,
                currentPath = "Initializing media scan...",
                scannedCount = 0,
                isCompleted = false
            )
        }

        scanJob = scope.launch {
            try {
                performScan()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                _scanProgress.update {
                    ScanProgress(
                        isScanning = false,
                        currentPath = "",
                        scannedCount = it.scannedCount,
                        isCompleted = true
                    )
                }
            }
        }
    }

    override fun resetScanState() {
        scanJob?.cancel()
        _scanProgress.value = ScanProgress()
    }

    private suspend fun performScan() = withContext(Dispatchers.IO) {
        val discoveredFolders = mutableSetOf<String>()
        val mediaStoreSongs = mutableListOf<SongEntity>()

        // 1. Scan storage directories and trigger MediaScanner Connection where applicable
        val defaultAudioDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS)
        )

        for (dir in defaultAudioDirs) {
            if (dir.exists() && dir.isDirectory) {
                discoveredFolders.add(dir.absolutePath)
                _scanProgress.update {
                    it.copy(currentPath = dir.absolutePath)
                }
                delay(30)
            }
        }

        // Trigger system media scanner for directories
        if (discoveredFolders.isNotEmpty()) {
            MediaScannerConnection.scanFile(
                context,
                discoveredFolders.toTypedArray(),
                arrayOf("audio/*"),
                null
            )
        }

        // 2. Query MediaStore for audio files and update scanning progress dynamically
        val settings = runCatching { settingsRepository.getSettings().first() }.getOrNull()
        val excludedFolders = settings?.excludedFolders ?: emptySet()
        val minTrackDurationMs = (settings?.minTrackDurationSeconds ?: 30) * 1000L

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val projection = arrayOf(
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

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            var count = 0
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

                count++
                val rawDateAdded = if (dateAddedCol != -1) cursor.getLong(dateAddedCol) else 0L
                val dateAdded = if (rawDateAdded > 0) rawDateAdded * 1000L else System.currentTimeMillis()
                val (year, genre) = extractMetadataFromFile(path)

                mediaStoreSongs.add(
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

                _scanProgress.update {
                    it.copy(
                        currentPath = parentFolder,
                        scannedCount = count
                    )
                }

                if (count % 3 == 0) {
                    delay(20)
                }
            }
        }

        // 3. Merge scanned MediaStore entries with existing Room DB records
        val cachedSongs = songDao.getAllSongsOnce()
        val cachedMap = cachedSongs.associateBy { it.id }

        val mergedEntities = mediaStoreSongs.map { deviceEntity ->
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

        if (mergedEntities.isNotEmpty()) {
            songDao.upsertSongs(mergedEntities)
            songDao.deleteStaleSongs(mergedEntities.map { it.path })
        }

        delay(100) // Brief pause to complete final progress state cleanly

        // 4. Update scan progress state to completed
        _scanProgress.update {
            ScanProgress(
                isScanning = false,
                currentPath = "",
                scannedCount = mediaStoreSongs.size,
                isCompleted = true
            )
        }
    }

    private fun extractMetadataFromFile(path: String): Pair<String, String> {
        if (path.isEmpty()) return Pair("", "")
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
}
