package com.sftwr.cleandevelopment.musicplayer.music_player.widget.di

import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetCurrentlyPlayingUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSyncedLyricsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SeekToUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SkipToNextUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SkipToPreviousUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ToggleLikeUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.TogglePlayPauseUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ToggleShuffleUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint interface for Glance widget action callbacks instantiated reflectively by system_server.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getCurrentlyPlayingUseCase(): GetCurrentlyPlayingUseCase
    fun togglePlayPauseUseCase(): TogglePlayPauseUseCase
    fun skipToNextUseCase(): SkipToNextUseCase
    fun skipToPreviousUseCase(): SkipToPreviousUseCase
    fun toggleShuffleUseCase(): ToggleShuffleUseCase
    fun toggleLikeUseCase(): ToggleLikeUseCase
    fun getSyncedLyricsUseCase(): GetSyncedLyricsUseCase
    fun seekToUseCase(): SeekToUseCase
}
