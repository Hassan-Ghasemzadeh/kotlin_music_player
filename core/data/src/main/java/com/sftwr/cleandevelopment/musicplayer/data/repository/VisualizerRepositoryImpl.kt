package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sftwr.cleandevelopment.musicplayer.domain.model.VisualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.VisualizerRepository
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.visualizerDataStore: DataStore<Preferences> by preferencesDataStore(name = "visualizer_preferences")

@Singleton
class VisualizerRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : VisualizerRepository {

    private object Keys {
        val SELECTED_STYLE_ID = stringPreferencesKey("selected_visualizer_style_id")
        val VISUALIZER_ENABLED = booleanPreferencesKey("visualizer_enabled")
        val AUDIO_SENSITIVITY = floatPreferencesKey("audio_sensitivity")
    }

    override fun getVisualizerState(): Flow<VisualizerState> {
        return context.visualizerDataStore.data.map { preferences ->
            val styleId = preferences[Keys.SELECTED_STYLE_ID] ?: VisualizerStyle.FANTASY_PLANET.id
            val isEnabled = preferences[Keys.VISUALIZER_ENABLED] ?: true
            val sensitivity = preferences[Keys.AUDIO_SENSITIVITY] ?: 1.0f

            VisualizerState(
                selectedStyle = VisualizerStyle.fromId(styleId),
                isEnabled = isEnabled,
                audioSensitivity = sensitivity
            )
        }
    }

    override suspend fun setSelectedVisualizer(style: VisualizerStyle) {
        context.visualizerDataStore.edit { preferences ->
            preferences[Keys.SELECTED_STYLE_ID] = style.id
        }
    }

    override suspend fun setVisualizerEnabled(enabled: Boolean) {
        context.visualizerDataStore.edit { preferences ->
            preferences[Keys.VISUALIZER_ENABLED] = enabled
        }
    }

    override suspend fun setAudioSensitivity(sensitivity: Float) {
        context.visualizerDataStore.edit { preferences ->
            preferences[Keys.AUDIO_SENSITIVITY] = sensitivity
        }
    }
}
