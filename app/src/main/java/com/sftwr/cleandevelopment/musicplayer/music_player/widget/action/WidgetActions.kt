package com.sftwr.cleandevelopment.musicplayer.music_player.widget.action

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.di.WidgetEntryPoint
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.util.WidgetStateUpdater
import dagger.hilt.android.EntryPointAccessors

/**
 * ActionCallback to toggle Play/Pause state.
 */
class TogglePlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            entryPoint.togglePlayPauseUseCase().invoke()
            WidgetStateUpdater.updateAllWidgets(context)
        }
    }
}

/**
 * ActionCallback to skip to the next track.
 */
class SkipNextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val currentState = entryPoint.getCurrentlyPlayingUseCase().getDirect()
            if (currentState.hasNext) {
                entryPoint.skipToNextUseCase().invoke()
            }
            WidgetStateUpdater.updateAllWidgets(context)
        }
    }
}
typealias NextActionCallback = SkipNextAction

/**
 * ActionCallback to skip to the previous track.
 */
class SkipPreviousAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val currentState = entryPoint.getCurrentlyPlayingUseCase().getDirect()
            if (currentState.hasPrevious) {
                entryPoint.skipToPreviousUseCase().invoke()
            }
            WidgetStateUpdater.updateAllWidgets(context)
        }
    }
}
typealias PreviousActionCallback = SkipPreviousAction

/**
 * ActionCallback to toggle Shuffle mode.
 */
class ToggleShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            entryPoint.toggleShuffleUseCase().invoke()
            WidgetStateUpdater.updateAllWidgets(context)
        }
    }
}

/**
 * ActionCallback to toggle Like / Favorite status.
 */
class ToggleLikeAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            entryPoint.toggleLikeUseCase().invoke()
            WidgetStateUpdater.updateAllWidgets(context)
        }
    }
}

/**
 * ActionCallback to seek to a specific position in milliseconds.
 */
class SeekToPositionAction : ActionCallback {
    companion object {
        val KEY_POSITION_MS = ActionParameters.Key<Long>("seek_position_ms")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        runCatching {
            val positionMs = parameters[KEY_POSITION_MS] ?: return
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            entryPoint.seekToUseCase().invoke(positionMs)
            WidgetStateUpdater.updateAllWidgets(context)
        }
    }
}
