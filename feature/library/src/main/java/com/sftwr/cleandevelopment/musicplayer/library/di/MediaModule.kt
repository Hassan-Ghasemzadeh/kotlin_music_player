package com.sftwr.cleandevelopment.musicplayer.library.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    /**
     * Provides standard audio attributes configured for music playback.
     */
    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    /**
     * Provides primary ExoPlayer instance for audio playback.
     * Note: handleAudioFocus is false because PlayerManager manages system Audio Focus
     * at the application level, allowing dual-player crossfade overlap without focus conflict.
     */
    @Provides
    @Named("playerA")
    @Singleton
    fun providePlayerA(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, false)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    /**
     * Provides secondary ExoPlayer instance for crossfade playback overlap.
     */
    @Provides
    @Named("playerB")
    @Singleton
    fun providePlayerB(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, false)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    /**
     * Provides a singleton instance of Player wrapped via PlayerManager.
     */
    @Provides
    @Singleton
    fun providePlayer(
        playerManager: PlayerManager
    ): Player = playerManager.exoPlayer

    @Provides
    @Singleton
    fun providePlayerController(
        playerManager: PlayerManager
    ): com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController = playerManager
}
