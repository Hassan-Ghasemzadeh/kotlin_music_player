package com.sftwr.cleandevelopment.musicplayer.data

import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import com.sftwr.cleandevelopment.musicplayer.data.repository.MusicRepositoryImpl
import com.sftwr.cleandevelopment.musicplayer.data.source.local.MusicDatabase
import com.sftwr.cleandevelopment.musicplayer.data.util.AudioTagEditor
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * Instrumented performance benchmark test measuring execution time for editing a single
 * song's tags when the Room database and repository contain 200 tracks.
 */
@RunWith(AndroidJUnit4::class)
class SongTagEditBenchmarkTest {

    private lateinit var database: MusicDatabase
    private lateinit var repository: MusicRepositoryImpl
    private lateinit var targetFile: File

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // 1. Initialize in-memory Room database for fast & isolated benchmark execution
        database = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val songDao = database.songDao()
        val audioTagEditor = AudioTagEditor()

        repository = MusicRepositoryImpl(
            context = context,
            audioTagEditor = audioTagEditor,
            songDao = songDao
        )

        // 2. Copy a valid small sample MP3 file from androidTest assets for real file I/O
        targetFile = File(context.cacheDir, "benchmark_target_song.mp3")
        try {
            context.assets.open("sample.mp3").use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // Fallback if asset file is missing
            targetFile.writeBytes(ByteArray(1024 * 100))
        }

        // 3. Setup test scenario with 200 mock songs inserted into Room database
        val mockSongs = (1L..200L).map { id ->
            SongEntity(
                id = id,
                title = "Track $id",
                artist = "Artist $id",
                album = "Album $id",
                albumId = id,
                duration = 180000L,
                size = 5000000L,
                path = if (id == 100L) targetFile.absolutePath else "/storage/emulated/0/Music/track_$id.mp3",
                year = "2023",
                genre = "Pop"
            )
        }

        runBlocking {
            songDao.upsertSongs(mockSongs)
        }
    }

    @After
    fun tearDown() {
        database.close()
        if (::targetFile.isInitialized && targetFile.exists()) {
            targetFile.delete()
        }
    }

    @Test
    fun benchmarkEditSongTags_with200SongsInDatabase_measuresExecutionTime() {
        val targetSongId = 100L
        val newTitle = "Benchmark Updated Title"
        val newArtist = "Benchmark Updated Artist"
        val newAlbum = "Benchmark Updated Album"
        val newYear = "2025"
        val newGenre = "Rock"

        // Measure exact execution time (in milliseconds)
        val elapsedTimeMs = measureTimeMillis {
            runBlocking {
                val result = repository.updateSongTags(
                    songId = targetSongId,
                    title = newTitle,
                    artist = newArtist,
                    album = newAlbum,
                    year = newYear,
                    genre = newGenre
                )

                // If it fails, print the error/exception message to Logcat for easier debugging
                if (result is UpdateTagResult.Error) {
                    Log.e("SongTagEditBenchmark", "Tag edit failed: ${result.message}", Exception(result.message))
                }

                assertTrue("Expected UpdateTagResult.Success, but got: $result", result is UpdateTagResult.Success)
            }
        }

        // Log elapsed execution time clearly
        Log.d("SongTagEditBenchmark", "=========================================================")
        Log.d("SongTagEditBenchmark", "Tag Edit Execution Time (200 tracks in DB): $elapsedTimeMs ms")
        Log.d("SongTagEditBenchmark", "=========================================================")
        println("[BENCHMARK] Tag Edit Execution Time (200 tracks in DB): $elapsedTimeMs ms")

        // Verify Room cache for target song was refreshed immediately
        val updatedSong = runBlocking { database.songDao().getAllSongsOnce() }.find { it.id == targetSongId }
        assertNotNull("Updated song entity should exist in Room DB", updatedSong)
        assertEquals(newTitle, updatedSong?.title)
        assertEquals(newArtist, updatedSong?.artist)
        assertEquals(newAlbum, updatedSong?.album)
        assertEquals(newYear, updatedSong?.year)
        assertEquals(newGenre, updatedSong?.genre)
    }
}