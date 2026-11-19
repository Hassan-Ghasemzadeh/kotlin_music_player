package com.sftwr.cleandevelopment.musicplayer.data.di

import com.sftwr.cleandevelopment.musicplayer.data.repository.WidgetMusicRepositoryImpl
import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetDataModule {

    @Binds
    @Singleton
    abstract fun bindWidgetMusicRepository(
        impl: WidgetMusicRepositoryImpl
    ): WidgetMusicRepository
}
