package com.sftwr.cleandevelopment.musicplayer.music_player.widget.util

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.sftwr.cleandevelopment.musicplayer.data.repository.WidgetPreferencesKeys
import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.lyrics.LyricTickerWidget
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.player.GlassmorphismPlayerWidget

/**
 * Utility helper responsible for updating Glance widget DataStore preferences across all active GlanceIds
 * and triggering immediate re-renders for both Glassmorphism Player Widget and Lyric Ticker Widget.
 */
object WidgetStateUpdater {

    suspend fun updateAllWidgets(context: Context, state: WidgetPlaybackState? = null) {
        runCatching {
            val manager = GlanceAppWidgetManager(context)
            val playerGlanceIds = manager.getGlanceIds(GlassmorphismPlayerWidget::class.java)
            val lyricGlanceIds = manager.getGlanceIds(LyricTickerWidget::class.java)
            val allGlanceIds = playerGlanceIds + lyricGlanceIds

            if (state != null) {
                for (glanceId in allGlanceIds) {
                    updateAppWidgetState(context, glanceId) { prefs ->
                        populatePreferences(prefs, state)
                    }
                }
            } else {
                for (glanceId in allGlanceIds) {
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[WidgetPreferencesKeys.KEY_REVISION] = System.currentTimeMillis()
                    }
                }
            }

            GlassmorphismPlayerWidget().updateAll(context)
            LyricTickerWidget().updateAll(context)
        }
    }

    private fun populatePreferences(prefs: MutablePreferences, state: WidgetPlaybackState) {
        state.currentSong?.let { song ->
            prefs[WidgetPreferencesKeys.KEY_SONG_ID] = song.id
            prefs[WidgetPreferencesKeys.KEY_TRACK_TITLE] = song.title
            prefs[WidgetPreferencesKeys.KEY_ARTIST_NAME] = song.artist
            prefs[WidgetPreferencesKeys.KEY_ALBUM_NAME] = song.album
            prefs[WidgetPreferencesKeys.KEY_ALBUM_ART_URI] = song.albumArtUri ?: ""
        }
        prefs[WidgetPreferencesKeys.KEY_IS_PLAYING] = state.isPlaying
        prefs[WidgetPreferencesKeys.KEY_CURRENT_POSITION] = state.currentPositionMs
        prefs[WidgetPreferencesKeys.KEY_DURATION] = state.durationMs
        prefs[WidgetPreferencesKeys.KEY_IS_SHUFFLE] = state.isShuffleEnabled
        prefs[WidgetPreferencesKeys.KEY_IS_FAVORITE] = state.isFavorite
        prefs[WidgetPreferencesKeys.KEY_HAS_NEXT] = state.hasNext
        prefs[WidgetPreferencesKeys.KEY_HAS_PREVIOUS] = state.hasPrevious
        prefs[WidgetPreferencesKeys.KEY_REVISION] = state.lastUpdatedTimestamp
        prefs[WidgetPreferencesKeys.KEY_ACTIVE_LYRIC_INDEX] = state.currentLyricIndex

        val lyricContent = state.lyrics?.content
        if (!lyricContent.isNullOrBlank()) {
            prefs[WidgetPreferencesKeys.KEY_LYRICS_JSON] = lyricContent
        } else {
            prefs.remove(WidgetPreferencesKeys.KEY_LYRICS_JSON)
        }
    }
}
