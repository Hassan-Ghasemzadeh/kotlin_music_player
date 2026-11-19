package com.sftwr.cleandevelopment.musicplayer.music_player.widget.util

import android.content.Context
import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState
import com.sftwr.cleandevelopment.musicplayer.domain.widget.WidgetStateNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetStateNotifierImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : WidgetStateNotifier {

    override suspend fun updateAllWidgets(state: WidgetPlaybackState?) {
        WidgetStateUpdater.updateAllWidgets(context, state)
    }
}
