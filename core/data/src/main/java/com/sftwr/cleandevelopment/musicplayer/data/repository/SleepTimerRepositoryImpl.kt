package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sftwr.cleandevelopment.musicplayer.data.worker.SleepTimerWorker
import com.sftwr.cleandevelopment.musicplayer.domain.model.SleepTimerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sleepTimerDataStore: DataStore<Preferences> by preferencesDataStore(name = "sleep_timer_preferences")

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SleepTimerRepository {

    private object PreferencesKeys {
        val IS_ACTIVE = booleanPreferencesKey("is_active")
        val TARGET_END_TIME_MS = longPreferencesKey("target_end_time_ms")
        val DURATION_MINUTES = intPreferencesKey("duration_minutes")
    }

    override fun getSleepTimerState(): Flow<SleepTimerState> {
        return context.sleepTimerDataStore.data.map { preferences ->
            val isActive = preferences[PreferencesKeys.IS_ACTIVE] ?: false
            val targetEndTimeMs = preferences[PreferencesKeys.TARGET_END_TIME_MS] ?: 0L
            val durationMinutes = preferences[PreferencesKeys.DURATION_MINUTES] ?: 0

            val now = System.currentTimeMillis()
            val remainingMs = (targetEndTimeMs - now).coerceAtLeast(0L)

            if (isActive && remainingMs > 0L) {
                SleepTimerState(
                    isActive = true,
                    targetEndTimeMs = targetEndTimeMs,
                    durationMinutes = durationMinutes,
                    remainingTimeMs = remainingMs
                )
            } else {
                SleepTimerState(
                    isActive = false,
                    targetEndTimeMs = 0L,
                    durationMinutes = 0,
                    remainingTimeMs = 0L
                )
            }
        }.flatMapLatest { baseState ->
            flow {
                if (!baseState.isActive) {
                    emit(baseState)
                } else {
                    while (true) {
                        val now = System.currentTimeMillis()
                        val remainingMs = (baseState.targetEndTimeMs - now).coerceAtLeast(0L)
                        if (remainingMs <= 0L) {
                            emit(SleepTimerState(isActive = false))
                            break
                        }
                        emit(baseState.copy(remainingTimeMs = remainingMs))
                        delay(1000L)
                    }
                }
            }
        }.distinctUntilChanged()
    }

    override suspend fun setSleepTimer(durationMinutes: Int) {
        if (durationMinutes <= 0) {
            cancelSleepTimer()
            return
        }

        val targetEndTimeMs = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)

        context.sleepTimerDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ACTIVE] = true
            preferences[PreferencesKeys.TARGET_END_TIME_MS] = targetEndTimeMs
            preferences[PreferencesKeys.DURATION_MINUTES] = durationMinutes
        }

        val workManager = WorkManager.getInstance(context)
        val initialDelayMs = (targetEndTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)

        val workRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .addTag(SleepTimerWorker.WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            SleepTimerWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override suspend fun cancelSleepTimer() {
        context.sleepTimerDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ACTIVE] = false
            preferences[PreferencesKeys.TARGET_END_TIME_MS] = 0L
            preferences[PreferencesKeys.DURATION_MINUTES] = 0
        }

        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(SleepTimerWorker.WORK_NAME)
    }

    override suspend fun getRemainingTimeMs(): Long {
        var remainingMs = 0L
        context.sleepTimerDataStore.edit { preferences ->
            val isActive = preferences[PreferencesKeys.IS_ACTIVE] ?: false
            val targetEndTimeMs = preferences[PreferencesKeys.TARGET_END_TIME_MS] ?: 0L
            if (isActive) {
                remainingMs = (targetEndTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)
            }
        }
        return remainingMs
    }
}
