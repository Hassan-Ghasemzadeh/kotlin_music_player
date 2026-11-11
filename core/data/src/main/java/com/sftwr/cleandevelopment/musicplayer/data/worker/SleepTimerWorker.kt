package com.sftwr.cleandevelopment.musicplayer.data.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SleepTimerWorkerEntryPoint {
    fun sleepTimerRepository(): SleepTimerRepository
}

/**
 * WorkManager CoroutineWorker responsible for triggering music playback stoppage
 * and resetting the Sleep Timer state when the timer duration expires in background or foreground.
 */
class SleepTimerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SleepTimerWorkerEntryPoint::class.java
            )
            val sleepTimerRepository = entryPoint.sleepTimerRepository()

            // Reset sleep timer state in DataStore
            sleepTimerRepository.cancelSleepTimer()

            // Broadcast intent to stop/pause music playback across active components/services
            val stopIntent = Intent(ACTION_STOP_SLEEP_TIMER_PLAYBACK).apply {
                setPackage(applicationContext.packageName)
            }
            applicationContext.sendBroadcast(stopIntent)

            Result.success()
        }.getOrElse {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "SleepTimerWorkerJob"
        const val ACTION_STOP_SLEEP_TIMER_PLAYBACK = "com.sftwr.cleandevelopment.musicplayer.ACTION_STOP_SLEEP_TIMER"
    }
}
