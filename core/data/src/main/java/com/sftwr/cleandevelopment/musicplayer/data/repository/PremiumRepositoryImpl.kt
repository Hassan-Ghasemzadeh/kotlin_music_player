package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PremiumRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "music_player_premium_prefs")

/**
 * DataStore-backed implementation of [PremiumRepository].
 */
@Singleton
class PremiumRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PremiumRepository {

    private object PreferencesKeys {
        val IS_AD_FREE = booleanPreferencesKey("is_ad_free")
    }

    override val isAdFree: Flow<Boolean> = context.premiumDataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_AD_FREE] ?: false
    }

    override suspend fun setAdFree(isAdFree: Boolean) {
        context.premiumDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AD_FREE] = isAdFree
        }
    }

    override suspend fun togglePremium() {
        val current = isAdFree.first()
        setAdFree(!current)
    }
}
