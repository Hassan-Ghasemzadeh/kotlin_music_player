package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.model.isFree
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.equalizerDataStore: DataStore<Preferences> by preferencesDataStore(name = "equalizer_preferences")

@Singleton
class EqualizerRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : EqualizerRepository {

    private object Keys {
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val PRESET_ID = intPreferencesKey("preset_id")
        val PRESET_NAME = stringPreferencesKey("preset_name")
        val BAND_LEVELS = stringPreferencesKey("band_levels")
        val BASS_BOOST = intPreferencesKey("bass_boost_strength")
        val VIRTUALIZER = intPreferencesKey("virtualizer_strength")
        val UNLOCKED_PRESET_ID = intPreferencesKey("unlocked_preset_id")
    }

    override fun getEqualizerState(): Flow<EqualizerState> {
        return context.equalizerDataStore.data.map { preferences ->
            val isEnabled = preferences[Keys.EQUALIZER_ENABLED] ?: false
            val presetId = (preferences[Keys.PRESET_ID] ?: 0).toShort()
            val presetName = preferences[Keys.PRESET_NAME] ?: "Normal"
            val bassBoost = (preferences[Keys.BASS_BOOST] ?: 0).toShort()
            val virtualizer = (preferences[Keys.VIRTUALIZER] ?: 0).toShort()
            val rawBandsStr = preferences[Keys.BAND_LEVELS] ?: ""
            val unlockedPresetId = preferences[Keys.UNLOCKED_PRESET_ID]?.toShort()

            val savedBandLevels = parseBandLevels(rawBandsStr)
            val updatedBands = EqualizerPreset.DEFAULT_BANDS.map { band ->
                val level = savedBandLevels[band.index] ?: band.currentLevelMb
                band.copy(currentLevelMb = level)
            }

            EqualizerState(
                isEnabled = isEnabled,
                currentPresetId = presetId,
                currentPresetName = presetName,
                bands = updatedBands,
                bassBoostStrength = bassBoost,
                virtualizerStrength = virtualizer,
                availablePresets = EqualizerPreset.DEFAULT_PRESETS,
                unlockedPresetId = unlockedPresetId
            )
        }
    }

    override suspend fun saveEqualizerState(state: EqualizerState) {
        context.equalizerDataStore.edit { preferences ->
            preferences[Keys.EQUALIZER_ENABLED] = state.isEnabled
            preferences[Keys.PRESET_ID] = state.currentPresetId.toInt()
            preferences[Keys.PRESET_NAME] = state.currentPresetName
            preferences[Keys.BAND_LEVELS] = serializeBandLevels(state.bands.associate { it.index to it.currentLevelMb })
            preferences[Keys.BASS_BOOST] = state.bassBoostStrength.toInt()
            preferences[Keys.VIRTUALIZER] = state.virtualizerStrength.toInt()
            val unlockedId = state.unlockedPresetId
            if (unlockedId != null) {
                preferences[Keys.UNLOCKED_PRESET_ID] = unlockedId.toInt()
            } else {
                preferences.remove(Keys.UNLOCKED_PRESET_ID)
            }
        }
    }

    override suspend fun setEnabled(enabled: Boolean) {
        context.equalizerDataStore.edit { preferences ->
            preferences[Keys.EQUALIZER_ENABLED] = enabled
        }
    }

    override suspend fun setPreset(preset: EqualizerPreset) {
        context.equalizerDataStore.edit { preferences ->
            preferences[Keys.PRESET_ID] = preset.id.toInt()
            preferences[Keys.PRESET_NAME] = preset.displayName
            if (preset.bandLevels.isNotEmpty()) {
                val rawBandsStr = preferences[Keys.BAND_LEVELS] ?: ""
                val levelsMap = parseBandLevels(rawBandsStr).toMutableMap()
                preset.bandLevels.forEach { (bandIdx, level) ->
                    levelsMap[bandIdx] = level
                }
                preferences[Keys.BAND_LEVELS] = serializeBandLevels(levelsMap)
            }
            // If switching to a free preset, lock/clear previous paid preset unlock
            if (preset.isFree()) {
                preferences.remove(Keys.UNLOCKED_PRESET_ID)
            }
        }
    }

    override suspend fun setBandLevel(bandIndex: Short, levelMb: Short) {
        context.equalizerDataStore.edit { preferences ->
            val rawBandsStr = preferences[Keys.BAND_LEVELS] ?: ""
            val levelsMap = parseBandLevels(rawBandsStr).toMutableMap()
            levelsMap[bandIndex] = levelMb

            preferences[Keys.BAND_LEVELS] = serializeBandLevels(levelsMap)
            // When band level is changed manually, switch preset to Custom (-1) and lock paid presets
            preferences[Keys.PRESET_ID] = EqualizerPreset.CUSTOM.id.toInt()
            preferences[Keys.PRESET_NAME] = EqualizerPreset.CUSTOM.displayName
            preferences.remove(Keys.UNLOCKED_PRESET_ID)
        }
    }

    override suspend fun setBassBoostStrength(strength: Short) {
        context.equalizerDataStore.edit { preferences ->
            preferences[Keys.BASS_BOOST] = strength.toInt()
        }
    }

    override suspend fun setVirtualizerStrength(strength: Short) {
        context.equalizerDataStore.edit { preferences ->
            preferences[Keys.VIRTUALIZER] = strength.toInt()
        }
    }

    override suspend fun unlockPreset(presetId: Short) {
        context.equalizerDataStore.edit { preferences ->
            preferences[Keys.UNLOCKED_PRESET_ID] = presetId.toInt()
        }
    }

    override suspend fun lockPresets() {
        context.equalizerDataStore.edit { preferences ->
            preferences.remove(Keys.UNLOCKED_PRESET_ID)
        }
    }

    private fun parseBandLevels(raw: String): Map<Short, Short> {
        if (raw.isBlank()) return emptyMap()
        return runCatching {
            raw.split(",")
                .mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        parts[0].toShort() to parts[1].toShort()
                    } else null
                }.toMap()
        }.getOrDefault(emptyMap())
    }

    private fun serializeBandLevels(map: Map<Short, Short>): String {
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}
