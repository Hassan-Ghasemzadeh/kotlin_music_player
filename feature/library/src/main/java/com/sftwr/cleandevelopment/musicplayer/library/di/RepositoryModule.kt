package com.sftwr.cleandevelopment.musicplayer.library.di


import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.library.data.repository.MusicRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl,
    ): MusicRepository
}