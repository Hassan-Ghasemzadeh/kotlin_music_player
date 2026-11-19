package com.sftwr.cleandevelopment.musicplayer.music_player.widget.player

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * BroadcastReceiver for Widget 1 (Glassmorphism Music Player Widget).
 */
class GlassmorphismWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlassmorphismPlayerWidget()
}
