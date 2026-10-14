package com.sftwr.cleandevelopment.musicplayer.music_player

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class annotated with @HiltAndroidApp to trigger
 * Hilt's code generation and dependency container lifecycle.
 */
@HiltAndroidApp
class MusicPlayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

    }
}