package com.sftwr.cleandevelopment.musicplayer.library.di

import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
import com.sftwr.cleandevelopment.musicplayer.data.repository.MusicRepositoryImpl
import com.sftwr.cleandevelopment.musicplayer.data.repository.SongPreferencesRepositoryImpl
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
}