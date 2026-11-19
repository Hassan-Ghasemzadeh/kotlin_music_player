package com.sftwr.cleandevelopment.musicplayer.music_player.widget.di

import com.sftwr.cleandevelopment.musicplayer.domain.widget.WidgetStateNotifier
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.util.WidgetStateNotifierImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun bindWidgetStateNotifier(
        impl: WidgetStateNotifierImpl
    ): WidgetStateNotifier
}
