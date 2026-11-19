package com.sftwr.cleandevelopment.musicplayer.music_player.widget.lyrics

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * BroadcastReceiver for Widget 2 (Scrolling Lyric Ticker Widget).
 */
class LyricTickerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LyricTickerWidget()
}
