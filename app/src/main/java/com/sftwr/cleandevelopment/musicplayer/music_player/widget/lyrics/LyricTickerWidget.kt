package com.sftwr.cleandevelopment.musicplayer.music_player.widget.lyrics

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sftwr.cleandevelopment.musicplayer.data.repository.WidgetPreferencesKeys
import com.sftwr.cleandevelopment.musicplayer.data.util.LrcParser
import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import com.sftwr.cleandevelopment.musicplayer.model.LyricLine
import com.sftwr.cleandevelopment.musicplayer.music_player.MainActivity
import com.sftwr.cleandevelopment.musicplayer.music_player.R
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.action.SkipNextAction
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.action.SkipPreviousAction
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.action.ToggleLikeAction
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.action.TogglePlayPauseAction
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.action.ToggleShuffleAction
import com.sftwr.cleandevelopment.musicplayer.music_player.widget.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors

/**
 * Widget 2: Scrolling Lyric Ticker Widget (Vertical / Medium Size)
 * Features a dark translucent frosted-glass container, time-synced lyrics with active line
 * highlighting and auto-scroll window, minimalist playback controls, and deep-linking to full-screen player.
 */
class LyricTickerWidget : GlanceAppWidget() {

    companion object {
        private val VERTICAL_SIZE = DpSize(180.dp, 240.dp)
        private val MEDIUM_SIZE = DpSize(240.dp, 280.dp)
        private val RESPONSIVE_SIZES = setOf(VERTICAL_SIZE, MEDIUM_SIZE)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(RESPONSIVE_SIZES)
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        val directState = entryPoint.getCurrentlyPlayingUseCase().getDirect()

        provideContent {
            val prefs = currentState<Preferences>()
            val isPlaying = prefs[WidgetPreferencesKeys.KEY_IS_PLAYING] ?: directState.isPlaying
            val posMs = prefs[WidgetPreferencesKeys.KEY_CURRENT_POSITION] ?: directState.currentPositionMs
            val durMs = prefs[WidgetPreferencesKeys.KEY_DURATION] ?: directState.durationMs
            val isShuffle = prefs[WidgetPreferencesKeys.KEY_IS_SHUFFLE] ?: directState.isShuffleEnabled
            val isFav = prefs[WidgetPreferencesKeys.KEY_IS_FAVORITE] ?: directState.isFavorite
            val hasNext = prefs[WidgetPreferencesKeys.KEY_HAS_NEXT] ?: directState.hasNext
            val hasPrevious = prefs[WidgetPreferencesKeys.KEY_HAS_PREVIOUS] ?: directState.hasPrevious
            val title = prefs[WidgetPreferencesKeys.KEY_TRACK_TITLE] ?: directState.currentSong?.title ?: "No Track Playing"
            val artist = prefs[WidgetPreferencesKeys.KEY_ARTIST_NAME] ?: directState.currentSong?.artist ?: "Unknown Artist"
            val songId = prefs[WidgetPreferencesKeys.KEY_SONG_ID] ?: directState.currentSong?.id ?: -1L
            val rawLyrics = prefs[WidgetPreferencesKeys.KEY_LYRICS_JSON]

            val activeLyrics = when {
                !rawLyrics.isNullOrBlank() -> {
                    val parsed = LrcParser.parse(rawLyrics)
                    val mappedLines = if (parsed.isSynced) {
                        parsed.lines
                    } else {
                        parsed.lines.mapIndexed { index, line ->
                            LyricLine(timeMs = index * 4000L, text = line.text)
                        }
                    }
                    Lyric(
                        songId = songId,
                        title = title,
                        artist = artist,
                        content = rawLyrics,
                        isSynced = parsed.isSynced,
                        lines = mappedLines
                    )
                }
                songId == directState.currentSong?.id -> directState.lyrics
                else -> null
            }

            val activeState = directState.copy(
                isPlaying = isPlaying,
                currentPositionMs = posMs,
                durationMs = durMs,
                isShuffleEnabled = isShuffle,
                isFavorite = isFav,
                hasNext = hasNext,
                hasPrevious = hasPrevious,
                lyrics = activeLyrics,
                currentSong = directState.currentSong?.copy(
                    id = songId,
                    title = title,
                    artist = artist,
                    isFavorite = isFav
                )
            )

            key(songId, isShuffle, isFav, hasNext, hasPrevious, activeLyrics?.lines?.size, activeState.lastUpdatedTimestamp) {
                LyricTickerContent(state = activeState)
            }
        }
    }

    @Composable
    private fun LyricTickerContent(state: WidgetPlaybackState) {
        val lines = state.lyrics?.lines ?: emptyList()
        val activeIndex = state.currentLyricIndex

        val windowStart = (activeIndex - 2).coerceAtLeast(0)
        val windowEnd = (activeIndex + 5).coerceAtMost(lines.size)
        val visibleLines = if (lines.isNotEmpty() && windowStart < lines.size) {
            lines.subList(windowStart, windowEnd)
        } else emptyList()

        val activeLineMs = lines.getOrNull(activeIndex.coerceIn(0, (lines.size - 1).coerceAtLeast(0)))?.timeMs ?: -1L

        // Outer Frosted Dark Glass Container
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xCC090C14)))
                .cornerRadius(20.dp)
                .padding(14.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // Header: Track Title & Artist
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    Text(
                        text = state.currentSong?.title ?: "No Track Playing",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = state.currentSong?.artist ?: "Unknown Artist",
                        style = TextStyle(
                            color = ColorProvider(Color(0x99FFFFFF)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Synced Lyric Ticker Scrollable Column or Missing Lyrics Fallback Card
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                ) {
                    if (visibleLines.isNotEmpty()) {
                        LazyColumn(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .clickable(actionStartActivity<MainActivity>())
                        ) {
                            itemsIndexed(
                                items = visibleLines,
                                itemId = { _, line -> line.timeMs }
                            ) { _, line ->
                                val isActive = line.timeMs == activeLineMs
                                LyricLineItem(
                                    line = line,
                                    isActive = isActive
                                )
                            }
                        }
                    } else {
                        // Missing Lyric Fallback Message Card
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .background(ColorProvider(Color(0x20FFFFFF)))
                                .cornerRadius(12.dp)
                                .padding(12.dp)
                                .clickable(actionStartActivity<MainActivity>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_widget_music_note),
                                    contentDescription = "No Lyrics",
                                    modifier = GlanceModifier.size(32.dp)
                                )
                                Spacer(modifier = GlanceModifier.height(8.dp))
                                Text(
                                    text = "No lyrics found for this song",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Text(
                                    text = "Tap to open app and add lyrics",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF80D8FF)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = state.progressRatio,
                    modifier = GlanceModifier.fillMaxWidth().height(4.dp).cornerRadius(2.dp),
                    color = ColorProvider(Color(0xFF80D8FF)),
                    backgroundColor = ColorProvider(Color(0x33FFFFFF))
                )

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Minimalist Controls Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like / Heart
                    Box(
                        modifier = GlanceModifier
                            .size(30.dp)
                            .clickable(actionRunCallback<ToggleLikeAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(
                                if (state.isFavorite) R.drawable.ic_widget_heart_filled else R.drawable.ic_widget_heart_outline
                            ),
                            contentDescription = "Like",
                            modifier = GlanceModifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Previous Track
                    Box(
                        modifier = GlanceModifier
                            .size(30.dp)
                            .background(
                                ColorProvider(
                                    if (state.hasPrevious) Color(0x33FFFFFF) else Color(0x0AFFFFFF)
                                )
                            )
                            .cornerRadius(15.dp)
                            .then(
                                if (state.hasPrevious) {
                                    GlanceModifier.clickable(actionRunCallback<SkipPreviousAction>())
                                } else {
                                    GlanceModifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_skip_previous),
                            contentDescription = "Previous",
                            modifier = GlanceModifier.size(18.dp),
                            colorFilter = ColorFilter.tint(
                                ColorProvider(
                                    if (state.hasPrevious) Color.White else Color(0x3DFFFFFF)
                                )
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Play / Pause Circular Button
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .background(ColorProvider(Color(0x33FFFFFF)))
                            .cornerRadius(18.dp)
                            .clickable(actionRunCallback<TogglePlayPauseAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(
                                if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                            ),
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            modifier = GlanceModifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Next Track
                    Box(
                        modifier = GlanceModifier
                            .size(30.dp)
                            .background(
                                ColorProvider(
                                    if (state.hasNext) Color(0x33FFFFFF) else Color(0x0AFFFFFF)
                                )
                            )
                            .cornerRadius(15.dp)
                            .then(
                                if (state.hasNext) {
                                    GlanceModifier.clickable(actionRunCallback<SkipNextAction>())
                                } else {
                                    GlanceModifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_skip_next),
                            contentDescription = "Next",
                            modifier = GlanceModifier.size(18.dp),
                            colorFilter = ColorFilter.tint(
                                ColorProvider(
                                    if (state.hasNext) Color.White else Color(0x3DFFFFFF)
                                )
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    // Shuffle Button
                    Box(
                        modifier = GlanceModifier
                            .size(30.dp)
                            .background(
                                ColorProvider(
                                    if (state.isShuffleEnabled) Color(0xFF00E5FF) else Color.Transparent
                                )
                            )
                            .cornerRadius(15.dp)
                            .clickable(actionRunCallback<ToggleShuffleAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_shuffle),
                            contentDescription = "Shuffle",
                            modifier = GlanceModifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LyricLineItem(line: LyricLine, isActive: Boolean) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .background(
                    ColorProvider(if (isActive) Color(0x3380D8FF) else Color.Transparent)
                )
                .cornerRadius(8.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = line.text,
                style = TextStyle(
                    color = ColorProvider(
                        if (isActive) Color(0xFF80D8FF) else Color(0x70FFFFFF)
                    ),
                    fontSize = if (isActive) 15.sp else 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}
