package com.sftwr.cleandevelopment.musicplayer.domain.widget

import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState

/**
 * Domain interface for notifying Glance widgets to update their DataStore preferences
 * and re-render UI across all active GlanceIds.
 */
interface WidgetStateNotifier {
    /**
     * Updates widget DataStore preferences and triggers re-rendering across all active Glance widgets.
     */
    suspend fun updateAllWidgets(state: WidgetPlaybackState? = null)
}
