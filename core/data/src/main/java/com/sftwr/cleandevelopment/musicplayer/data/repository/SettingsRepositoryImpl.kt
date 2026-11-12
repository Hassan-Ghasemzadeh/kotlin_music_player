package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppSettings
import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "music_player_app_settings")

/**
 * Production implementation of [SettingsRepository] backed by Jetpack DataStore Preferences.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val GLASSMORPHIC_ENABLED = booleanPreferencesKey("glassmorphic_enabled")
        val ALBUM_ARTWORK_ENABLED = booleanPreferencesKey("album_artwork_enabled")
        val SHOW_ALBUM_ART = booleanPreferencesKey("show_album_art")

        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val PLAYBACK_PITCH = floatPreferencesKey("playback_pitch")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val DEFAULT_EQ_PRESET = stringPreferencesKey("default_eq_preset")
        val CIRCULAR_VISUALIZER_TYPE = stringPreferencesKey("circular_visualizer_type")
        val REPLAY_GAIN_MODE = stringPreferencesKey("replay_gain_mode")

        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val MIN_TRACK_DURATION = intPreferencesKey("min_track_duration")

        val LOCKSCREEN_CONTROLS = booleanPreferencesKey("lockscreen_controls")
        val SCREEN_OFF_PLAYBACK = booleanPreferencesKey("screen_off_playback")
        val HEADSET_CONTROLS = booleanPreferencesKey("headset_controls")
        val ALLOW_EXTERNAL_DEVICE_PLAYBACK = booleanPreferencesKey("allow_external_device_playback")
        val SHAKE_TO_SKIP = booleanPreferencesKey("shake_to_skip")
    }

    override fun getSettings(): Flow<AppSettings> {
        return context.appSettingsDataStore.data.map { preferences ->
            val albumArtEnabled = preferences[PreferencesKeys.ALBUM_ARTWORK_ENABLED]
                ?: preferences[PreferencesKeys.SHOW_ALBUM_ART]
                ?: true
            AppSettings(
                language = AppLanguage.fromCode(preferences[PreferencesKeys.LANGUAGE] ?: "en"),
                glassmorphicStyleEnabled = preferences[PreferencesKeys.GLASSMORPHIC_ENABLED] ?: true,
                isAlbumArtworkEnabled = albumArtEnabled,
                playbackSpeed = preferences[PreferencesKeys.PLAYBACK_SPEED] ?: 1.0f,
                playbackPitch = preferences[PreferencesKeys.PLAYBACK_PITCH] ?: 1.0f,
                crossfadeDurationSeconds = preferences[PreferencesKeys.CROSSFADE_SECONDS] ?: 0,
                skipSilenceEnabled = preferences[PreferencesKeys.SKIP_SILENCE] ?: false,
                defaultEqualizerPreset = preferences[PreferencesKeys.DEFAULT_EQ_PRESET] ?: "Flat",
                circularVisualizerType = preferences[PreferencesKeys.CIRCULAR_VISUALIZER_TYPE] ?: "NEON_FREQUENCY_BARS",
                replayGainMode = ReplayGainMode.fromKey(preferences[PreferencesKeys.REPLAY_GAIN_MODE] ?: "off"),
                excludedFolders = preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: emptySet(),
                minTrackDurationSeconds = preferences[PreferencesKeys.MIN_TRACK_DURATION] ?: 30,
                lockscreenControlsEnabled = preferences[PreferencesKeys.LOCKSCREEN_CONTROLS] ?: true,
                screenOffPlaybackEnabled = preferences[PreferencesKeys.SCREEN_OFF_PLAYBACK] ?: true,
                headsetControlsEnabled = preferences[PreferencesKeys.HEADSET_CONTROLS] ?: true,
                allowExternalDevicePlayback = preferences[PreferencesKeys.ALLOW_EXTERNAL_DEVICE_PLAYBACK] ?: true,
                shakeToSkipEnabled = preferences[PreferencesKeys.SHAKE_TO_SKIP] ?: false
            )
        }
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.code
        }
    }

    override suspend fun updateGlassmorphicStyle(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.GLASSMORPHIC_ENABLED] = enabled
        }
    }

    override suspend fun updateAlbumArtworkEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ALBUM_ARTWORK_ENABLED] = enabled
            preferences[PreferencesKeys.SHOW_ALBUM_ART] = enabled
        }
    }

    override suspend fun updateShowAlbumArt(enabled: Boolean) {
        updateAlbumArtworkEnabled(enabled)
    }

    override suspend fun updatePlaybackSpeed(speed: Float) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYBACK_SPEED] = speed
        }
    }

    override suspend fun updatePlaybackPitch(pitch: Float) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYBACK_PITCH] = pitch
        }
    }

    override suspend fun updateCrossfadeDuration(seconds: Int) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.CROSSFADE_SECONDS] = seconds
        }
    }

    override suspend fun updateSkipSilence(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SKIP_SILENCE] = enabled
        }
    }

    override suspend fun updateDefaultEqualizerPreset(preset: String) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_EQ_PRESET] = preset
        }
    }

    override suspend fun updateCircularVisualizerType(typeKey: String) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.CIRCULAR_VISUALIZER_TYPE] = typeKey
        }
    }

    override suspend fun updateReplayGainMode(mode: ReplayGainMode) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.REPLAY_GAIN_MODE] = mode.key
        }
    }

    override suspend fun addExcludedFolder(path: String) {
        context.appSettingsDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: emptySet()
            preferences[PreferencesKeys.EXCLUDED_FOLDERS] = current + path
        }
    }

    override suspend fun removeExcludedFolder(path: String) {
        context.appSettingsDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.EXCLUDED_FOLDERS] ?: emptySet()
            preferences[PreferencesKeys.EXCLUDED_FOLDERS] = current - path
        }
    }

    override suspend fun updateMinTrackDuration(seconds: Int) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_TRACK_DURATION] = seconds
        }
    }

    override suspend fun updateLockscreenControls(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCKSCREEN_CONTROLS] = enabled
        }
    }

    override suspend fun updateScreenOffPlayback(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SCREEN_OFF_PLAYBACK] = enabled
        }
    }

    override suspend fun updateHeadsetControls(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.HEADSET_CONTROLS] = enabled
        }
    }

    override suspend fun updateAllowExternalDevicePlayback(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_EXTERNAL_DEVICE_PLAYBACK] = enabled
        }
    }

    override suspend fun updateShakeToSkip(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SHAKE_TO_SKIP] = enabled
        }
    }
}
