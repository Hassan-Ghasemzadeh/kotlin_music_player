package com.sftwr.cleandevelopment.musicplayer.library.di

import com.sftwr.cleandevelopment.musicplayer.data.repository.EqualizerRepositoryImpl
import com.sftwr.cleandevelopment.musicplayer.data.repository.MusicRepositoryImpl
import com.sftwr.cleandevelopment.musicplayer.data.repository.PlaylistRepositoryImpl
import com.sftwr.cleandevelopment.musicplayer.data.repository.SongPreferencesRepositoryImpl
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the implementation of MusicRepository to its interface.
     */
    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl,
    ): MusicRepository

    /**
     * Binds the implementation of SongPreferencesRepository to its interface.
     */
    @Binds
    @Singleton
    abstract fun bindSongPreferencesRepository(
        songPreferencesRepositoryImpl: SongPreferencesRepositoryImpl,
    ): SongPreferencesRepository

    /**
     * Binds the implementation of PlaylistRepository to its interface.
     */
    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl,
    ): PlaylistRepository

    /**
     * Binds the implementation of EqualizerRepository to its interface.
     */
    @Binds
    @Singleton
    abstract fun bindEqualizerRepository(
        equalizerRepositoryImpl: EqualizerRepositoryImpl,
    ): EqualizerRepository

    /**
     * Binds the implementation of SleepTimerRepository to its interface.
     */
    @Binds
    @Singleton
    abstract fun bindSleepTimerRepository(
        sleepTimerRepositoryImpl: com.sftwr.cleandevelopment.musicplayer.data.repository.SleepTimerRepositoryImpl,
    ): com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository

    /**
     * Binds the implementation of MediaScanRepository to its interface.
     */
    @Binds
    @Singleton
    abstract fun bindMediaScanRepository(
        mediaScanRepositoryImpl: com.sftwr.cleandevelopment.musicplayer.data.repository.MediaScanRepositoryImpl,
    ): com.sftwr.cleandevelopment.musicplayer.domain.repository.MediaScanRepository

    /**
     * Binds the implementation of PermissionRepository to its interface.
     */
    @Binds
    @Singleton
    abstract fun bindPermissionRepository(
        permissionRepositoryImpl: com.sftwr.cleandevelopment.musicplayer.data.repository.PermissionRepositoryImpl,
    ): com.sftwr.cleandevelopment.musicplayer.domain.repository.PermissionRepository
}
