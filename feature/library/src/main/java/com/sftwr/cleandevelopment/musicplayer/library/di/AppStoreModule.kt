package com.sftwr.cleandevelopment.musicplayer.library.di

import com.sftwr.cleandevelopment.musicplayer.data.launcher.BazaarStoreHandler
import com.sftwr.cleandevelopment.musicplayer.data.launcher.MyketStoreHandler
import com.sftwr.cleandevelopment.musicplayer.domain.launcher.AppStoreLauncher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AppStoreModule {

    @Binds
    @IntoSet
    abstract fun bindBazaarLauncher(impl: BazaarStoreHandler): AppStoreLauncher

    @Binds
    @IntoSet
    abstract fun bindMyketLauncher(impl: MyketStoreHandler): AppStoreLauncher
}
