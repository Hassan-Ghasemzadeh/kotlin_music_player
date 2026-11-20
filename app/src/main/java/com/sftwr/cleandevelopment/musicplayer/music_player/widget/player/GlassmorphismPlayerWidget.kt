package com.sftwr.cleandevelopment.musicplayer.music_player.widget.player

import android.content.Context
import android.graphics.Bitmap
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
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sftwr.cleandevelopment.musicplayer.data.repository.WidgetPreferencesKeys
import com.sftwr.cleandevelopment.musicplayer.data.util.WidgetBitmapUtils
import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState
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
 * Widget 1: Glassmorphism Music Player Widget (Medium / Large Size)
 * Features a deep, translucent frosted-glass background, dynamic album art downsampling,
 * real-time track metadata, dynamic linear progress, and interactive playback controls with tight vertical layout.
 */
class GlassmorphismPlayerWidget : GlanceAppWidget() {

    companion object {
        private val MEDIUM_SIZE = DpSize(240.dp, 160.dp)
        private val LARGE_SIZE = DpSize(320.dp, 200.dp)
        private val RESPONSIVE_SIZES = setOf(MEDIUM_SIZE, LARGE_SIZE)
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
            val album = prefs[WidgetPreferencesKeys.KEY_ALBUM_NAME] ?: directState.currentSong?.album ?: "Music Library"
            val artUri = prefs[WidgetPreferencesKeys.KEY_ALBUM_ART_URI] ?: directState.currentSong?.albumArtUri
            val songId = prefs[WidgetPreferencesKeys.KEY_SONG_ID] ?: directState.currentSong?.id ?: -1L
            val revision = prefs[WidgetPreferencesKeys.KEY_REVISION] ?: directState.lastUpdatedTimestamp

            val activeState = directState.copy(
                isPlaying = isPlaying,
                currentPositionMs = posMs,
                durationMs = durMs,
                isShuffleEnabled = isShuffle,
                isFavorite = isFav,
                hasNext = hasNext,
                hasPrevious = hasPrevious,
                lastUpdatedTimestamp = revision,
                currentSong = directState.currentSong?.copy(
                    id = songId,
                    title = title,
                    artist = artist,
                    album = album,
                    albumArtUri = artUri,
                    isFavorite = isFav
                )
            )

            val albumArtBitmap = WidgetBitmapUtils.decodeDownsampledWidgetBitmap(
                context = context,
                uriString = artUri,
                songId = songId,
                maxWidthDp = 120,
                maxHeightDp = 120
            )

            val progressPercent = (activeState.progressRatio * 100).toInt()
            val positionBucket = activeState.currentPositionMs / 1000

            key(
                songId,
                artUri,
                isPlaying,
                progressPercent,
                positionBucket,
                isShuffle,
                isFav,
                hasNext,
                hasPrevious,
                revision
            ) {
                GlassmorphismPlayerContent(
                    state = activeState,
                    albumArtBitmap = albumArtBitmap
                )
            }
        }
    }

    @Composable
    private fun GlassmorphismPlayerContent(
        state: WidgetPlaybackState,
        albumArtBitmap: Bitmap
    ) {
        val currentSize = LocalSize.current
        val isLarge = currentSize.width >= LARGE_SIZE.width

        // Outer Transparent Container Centered
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Tight Glassmorphic Card Hugging Content
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(ColorProvider(Color(0x8C0F141E)))
                    .cornerRadius(20.dp)
                    .padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Section: Album Art & Track Info Block
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dynamic Album Art / Vinyl Turntable
                        Box(
                            modifier = GlanceModifier
                                .size(if (isLarge) 68.dp else 56.dp)
                                .background(ColorProvider(Color(0x33FFFFFF)))
                                .cornerRadius(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(albumArtBitmap),
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = GlanceModifier.fillMaxSize().cornerRadius(12.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Track Metadata Column
                        Column(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.currentSong?.title ?: "No Track Playing",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = if (isLarge) 17.sp else 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = state.currentSong?.artist ?: "Unknown Artist",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xCCFFFFFF)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = state.currentSong?.album ?: "Music Library",
                                style = TextStyle(
                                    color = ColorProvider(Color(0x80FFFFFF)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Progress Bar & Percentage Row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = state.progressRatio,
                            modifier = GlanceModifier.defaultWeight().height(5.dp).cornerRadius(3.dp),
                            color = ColorProvider(Color.White),
                            backgroundColor = ColorProvider(Color(0x40FFFFFF))
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = state.progressPercentageText,
                            style = TextStyle(
                                color = ColorProvider(Color(0xB3FFFFFF)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Playback Controls Row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Track Button
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(
                                    ColorProvider(
                                        if (state.hasPrevious) Color(0x1FFFFFFF) else Color(0x0AFFFFFF)
                                    )
                                )
                                .cornerRadius(18.dp)
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
                                contentDescription = "Previous Track",
                                modifier = GlanceModifier.size(20.dp),
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(
                                        if (state.hasPrevious) Color.White else Color(0x3DFFFFFF)
                                    )
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Play / Pause Glass Button
                        Box(
                            modifier = GlanceModifier
                                .size(44.dp)
                                .background(ColorProvider(Color(0x40FFFFFF)))
                                .cornerRadius(22.dp)
                                .clickable(actionRunCallback<TogglePlayPauseAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(
                                    if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                                ),
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                modifier = GlanceModifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Next Track Button
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(
                                    ColorProvider(
                                        if (state.hasNext) Color(0x1FFFFFFF) else Color(0x0AFFFFFF)
                                    )
                                )
                                .cornerRadius(18.dp)
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
                                contentDescription = "Next Track",
                                modifier = GlanceModifier.size(20.dp),
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(
                                        if (state.hasNext) Color.White else Color(0x3DFFFFFF)
                                    )
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(14.dp))

                        // Shuffle Button
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(
                                    ColorProvider(
                                        if (state.isShuffleEnabled) Color(0xFF00E5FF) else Color(0x1FFFFFFF)
                                    )
                                )
                                .cornerRadius(18.dp)
                                .clickable(actionRunCallback<ToggleShuffleAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_shuffle),
                                contentDescription = "Shuffle",
                                modifier = GlanceModifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Like / Favorite Heart Button
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(ColorProvider(Color(0x1FFFFFFF)))
                                .cornerRadius(18.dp)
                                .clickable(actionRunCallback<ToggleLikeAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(
                                    if (state.isFavorite) R.drawable.ic_widget_heart_filled else R.drawable.ic_widget_heart_outline
                                ),
                                contentDescription = "Like Track",
                                modifier = GlanceModifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
