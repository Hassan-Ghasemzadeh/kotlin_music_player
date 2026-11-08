package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    override suspend fun clearLastPlayedSongId() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_PLAYED_SONG_ID)
        }
    }
}
