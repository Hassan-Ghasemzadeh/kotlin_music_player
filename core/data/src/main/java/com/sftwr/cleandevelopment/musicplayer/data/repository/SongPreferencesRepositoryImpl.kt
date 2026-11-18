package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sftwr.cleandevelopment.musicplayer.domain.model.LastPlayedSongState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_player_preferences")

/**
 * Implementation of [SongPreferencesRepository] using Jetpack DataStore Preferences
 * for asynchronous and non-blocking key-value storage.
 */
@Singleton
class SongPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SongPreferencesRepository {

    private object PreferencesKeys {
        val LAST_PLAYED_SONG_ID = stringPreferencesKey("last_played_song_id")
        val LAST_PLAYED_TITLE = stringPreferencesKey("last_played_title")
        val LAST_PLAYED_ARTIST = stringPreferencesKey("last_played_artist")
        val LAST_PLAYED_URI = stringPreferencesKey("last_played_uri")
        val LAST_PLAYED_DURATION = stringPreferencesKey("last_played_duration")
        val LAST_PLAYED_DURATION_MS = longPreferencesKey("last_played_duration_ms")
        val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
        val LAST_PLAYED_ALBUM = stringPreferencesKey("last_played_album")
        val LAST_PLAYED_ALBUM_ID = longPreferencesKey("last_played_album_id")
        val LAST_PLAYED_ALBUM_ART_URI = stringPreferencesKey("last_played_album_art_uri")
        val LAST_PLAYED_YEAR = stringPreferencesKey("last_played_year")
        val LAST_PLAYED_GENRE = stringPreferencesKey("last_played_genre")
    }

    override suspend fun saveLastPlayedSongId(songId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PLAYED_SONG_ID] = songId
        }
    }

    override suspend fun getLastPlayedSongId(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[PreferencesKeys.LAST_PLAYED_SONG_ID] }
            .firstOrNull()
    }

    override fun getLastPlayedSongIdFlow(): Flow<String?> {
        return context.dataStore.data
            .map { preferences -> preferences[PreferencesKeys.LAST_PLAYED_SONG_ID] }
    }

    override suspend fun saveLastPlayedSong(songState: LastPlayedSongState) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PLAYED_SONG_ID] = songState.songId
            preferences[PreferencesKeys.LAST_PLAYED_TITLE] = songState.title
            preferences[PreferencesKeys.LAST_PLAYED_ARTIST] = songState.artist
            preferences[PreferencesKeys.LAST_PLAYED_URI] = songState.uri
            preferences[PreferencesKeys.LAST_PLAYED_DURATION] = songState.duration
            preferences[PreferencesKeys.LAST_PLAYED_DURATION_MS] = songState.durationInMs
            preferences[PreferencesKeys.LAST_PLAYED_POSITION] = songState.positionMs
            preferences[PreferencesKeys.LAST_PLAYED_ALBUM] = songState.album
            preferences[PreferencesKeys.LAST_PLAYED_ALBUM_ID] = songState.albumId
            preferences[PreferencesKeys.LAST_PLAYED_ALBUM_ART_URI] = songState.albumArtUri
            preferences[PreferencesKeys.LAST_PLAYED_YEAR] = songState.year
            preferences[PreferencesKeys.LAST_PLAYED_GENRE] = songState.genre
        }
    }

    override suspend fun getLastPlayedSong(): LastPlayedSongState? {
        return getLastPlayedSongFlow().firstOrNull()
    }

    override fun getLastPlayedSongFlow(): Flow<LastPlayedSongState?> {
        return context.dataStore.data.map { preferences ->
            val id = preferences[PreferencesKeys.LAST_PLAYED_SONG_ID] ?: return@map null
            val title = preferences[PreferencesKeys.LAST_PLAYED_TITLE] ?: ""
            val artist = preferences[PreferencesKeys.LAST_PLAYED_ARTIST] ?: ""
            val uri = preferences[PreferencesKeys.LAST_PLAYED_URI] ?: ""
            val duration = preferences[PreferencesKeys.LAST_PLAYED_DURATION] ?: ""
            val durationInMs = preferences[PreferencesKeys.LAST_PLAYED_DURATION_MS] ?: 0L
            val positionMs = preferences[PreferencesKeys.LAST_PLAYED_POSITION] ?: 0L
            val album = preferences[PreferencesKeys.LAST_PLAYED_ALBUM] ?: ""
            val albumId = preferences[PreferencesKeys.LAST_PLAYED_ALBUM_ID] ?: 0L
            val albumArtUri = preferences[PreferencesKeys.LAST_PLAYED_ALBUM_ART_URI] ?: ""
            val year = preferences[PreferencesKeys.LAST_PLAYED_YEAR] ?: ""
            val genre = preferences[PreferencesKeys.LAST_PLAYED_GENRE] ?: ""

            LastPlayedSongState(
                songId = id,
                title = title,
                artist = artist,
                uri = uri,
                duration = duration,
                durationInMs = durationInMs,
                positionMs = positionMs,
                album = album,
                albumId = albumId,
                albumArtUri = albumArtUri,
                year = year,
                genre = genre
            )
        }
    }

    override suspend fun clearLastPlayedSongId() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_PLAYED_SONG_ID)
            preferences.remove(PreferencesKeys.LAST_PLAYED_TITLE)
            preferences.remove(PreferencesKeys.LAST_PLAYED_ARTIST)
            preferences.remove(PreferencesKeys.LAST_PLAYED_URI)
            preferences.remove(PreferencesKeys.LAST_PLAYED_DURATION)
            preferences.remove(PreferencesKeys.LAST_PLAYED_DURATION_MS)
            preferences.remove(PreferencesKeys.LAST_PLAYED_POSITION)
            preferences.remove(PreferencesKeys.LAST_PLAYED_ALBUM)
            preferences.remove(PreferencesKeys.LAST_PLAYED_ALBUM_ID)
            preferences.remove(PreferencesKeys.LAST_PLAYED_ALBUM_ART_URI)
            preferences.remove(PreferencesKeys.LAST_PLAYED_YEAR)
            preferences.remove(PreferencesKeys.LAST_PLAYED_GENRE)
        }
    }
}
