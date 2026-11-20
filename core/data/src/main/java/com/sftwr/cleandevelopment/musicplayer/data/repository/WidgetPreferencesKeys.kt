package com.sftwr.cleandevelopment.musicplayer.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore preferences keys used for persisting widget state and driving Glance updates.
 */
object WidgetPreferencesKeys {
    val KEY_SONG_ID = longPreferencesKey("widget_song_id")
    val KEY_TRACK_TITLE = stringPreferencesKey("widget_track_title")
    val KEY_ARTIST_NAME = stringPreferencesKey("widget_artist_name")
    val KEY_ALBUM_NAME = stringPreferencesKey("widget_album_name")
    val KEY_ALBUM_ART_URI = stringPreferencesKey("widget_album_art_uri")
    val KEY_IS_PLAYING = booleanPreferencesKey("widget_is_playing")
    val KEY_CURRENT_POSITION = longPreferencesKey("widget_current_position")
    val KEY_DURATION = longPreferencesKey("widget_duration")
    val KEY_IS_SHUFFLE = booleanPreferencesKey("widget_is_shuffle")
    val KEY_IS_FAVORITE = booleanPreferencesKey("widget_is_favorite")
    val KEY_HAS_NEXT = booleanPreferencesKey("widget_has_next")
    val KEY_HAS_PREVIOUS = booleanPreferencesKey("widget_has_previous")
    val KEY_LYRICS_JSON = stringPreferencesKey("widget_lyrics_json")
    val KEY_REVISION = longPreferencesKey("widget_revision")
    val KEY_ACTIVE_LYRIC_INDEX = intPreferencesKey("widget_active_lyric_index")
}
