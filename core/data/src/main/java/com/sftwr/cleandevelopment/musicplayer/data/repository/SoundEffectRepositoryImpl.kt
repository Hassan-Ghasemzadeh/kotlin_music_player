package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffect
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SoundEffectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.soundEffectDataStore: DataStore<Preferences> by preferencesDataStore(name = "sound_effect_preferences")

@Singleton
class SoundEffectRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SoundEffectRepository {

    private object Keys {
        val SOUND_EFFECT_ENABLED = booleanPreferencesKey("sound_effect_enabled")
        val CURRENT_EFFECT_ID = stringPreferencesKey("current_effect_id")
        val VOLUME_OR_BOOST = floatPreferencesKey("volume_or_boost")
    }

    override fun getSoundEffectState(): Flow<SoundEffectState> {
        return context.soundEffectDataStore.data.map { preferences ->
            val isEnabled = preferences[Keys.SOUND_EFFECT_ENABLED] ?: false
            val effectId = preferences[Keys.CURRENT_EFFECT_ID] ?: SoundEffect.ELECTRONIC_TUBE.id
            val volumeOrBoost = preferences[Keys.VOLUME_OR_BOOST] ?: 0.5f

            SoundEffectState(
                isEnabled = isEnabled,
                currentEffect = SoundEffect.fromId(effectId),
                volumeOrBoost = volumeOrBoost,
                availableEffects = SoundEffect.entries
            )
        }
    }

    override suspend fun saveSoundEffectState(state: SoundEffectState) {
        context.soundEffectDataStore.edit { preferences ->
            preferences[Keys.SOUND_EFFECT_ENABLED] = state.isEnabled
            preferences[Keys.CURRENT_EFFECT_ID] = state.currentEffect.id
            preferences[Keys.VOLUME_OR_BOOST] = state.volumeOrBoost
        }
    }

    override suspend fun setEnabled(enabled: Boolean) {
        context.soundEffectDataStore.edit { preferences ->
            preferences[Keys.SOUND_EFFECT_ENABLED] = enabled
        }
    }

    override suspend fun setCurrentEffect(effect: SoundEffect) {
        context.soundEffectDataStore.edit { preferences ->
            preferences[Keys.CURRENT_EFFECT_ID] = effect.id
        }
    }

    override suspend fun setVolumeOrBoost(value: Float) {
        context.soundEffectDataStore.edit { preferences ->
            preferences[Keys.VOLUME_OR_BOOST] = value
        }
    }
}
