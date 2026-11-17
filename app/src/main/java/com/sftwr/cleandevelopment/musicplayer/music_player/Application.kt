package com.sftwr.cleandevelopment.musicplayer.music_player

import android.app.Application
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Custom Application class annotated with @HiltAndroidApp to trigger
 * Hilt's code generation and dependency container lifecycle.
 */
@HiltAndroidApp
class MusicPlayerApplication : Application() {

    @Inject
    lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()
        // Initialize Tapsell Mediation SDK safely upon app startup
        adManager.initialize()
    }
}
