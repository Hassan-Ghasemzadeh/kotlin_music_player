package com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import java.util.Locale
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.home.R
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode.viewmodel.DriveModeViewModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val NeonBlueAccent = Color(0xFF91D8FB)
val DriveModeGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F0B21),
        Color(0xFF1B1238),
        Color(0xFF2C1454)
    )
)

/**
 * Stateful entry point route for Drive Mode.
 */
@Composable
fun DriveModeRoute(
    viewModel: DriveModeViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition(context, speechRecognizer, viewModel)
        } else {
            Toast.makeText(context, "Microphone permission is required for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    DriveModeScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onPlayPauseClick = viewModel::togglePlayPause,
        onSkipNextClick = viewModel::skipNext,
        onSkipPreviousClick = viewModel::skipPrevious,
        onFavoriteClick = viewModel::toggleFavorite,
        onVoiceAssistantClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition(context, speechRecognizer, viewModel)
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    )
}

private fun startVoiceRecognition(
    context: android.content.Context,
    speechRecognizer: SpeechRecognizer?,
    viewModel: DriveModeViewModel
) {
    if (speechRecognizer == null) {
        Toast.makeText(context, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
        return
    }

    val currentLang = viewModel.currentLanguage
    val languageTag = CommandMatcher.getBcp47LocaleTag(currentLang)

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageTag)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            viewModel.setListening(true)
            val listeningMsg = when (currentLang) {
                AppLanguage.PERSIAN -> "در حال گوش دادن به فرمان صوتی..."
                AppLanguage.GERMAN -> "Höre zu..."
                AppLanguage.TURKISH -> "Dinleniyor..."
                else -> "Listening for voice command..."
            }
            Toast.makeText(context, listeningMsg, Toast.LENGTH_SHORT).show()
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            viewModel.setListening(false)
        }
        override fun onError(error: Int) {
            viewModel.setListening(false)
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> when (currentLang) {
                    AppLanguage.PERSIAN -> "متوجه نشدم، دوباره تلاش کنید"
                    AppLanguage.GERMAN -> "Nicht verstanden, bitte erneut versuchen"
                    AppLanguage.TURKISH -> "Anlaşılamadı, tekrar deneyin"
                    else -> "Didn't catch that, tap to try again"
                }
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                else -> "Recognition error ($error)"
            }
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
        override fun onResults(results: Bundle?) {
            viewModel.setListening(false)
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val commandText = matches[0]
                val matchedCommand = CommandMatcher.match(commandText, currentLang)
                if (matchedCommand != PlayerCommand.UNKNOWN) {
                    viewModel.executeVoiceCommand(commandText)
                    val successMsg = when (currentLang) {
                        AppLanguage.PERSIAN -> "فرمان اجرا شد: \"$commandText\""
                        AppLanguage.GERMAN -> "Befehl ausgeführt: \"$commandText\""
                        AppLanguage.TURKISH -> "Komut uygulandı: \"$commandText\""
                        else -> "Command: \"$commandText\""
                    }
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                } else {
                    val unmatchedMsg = when (currentLang) {
                        AppLanguage.PERSIAN -> "فرمان در زبان انتخابی شناخته نشد: \"$commandText\""
                        AppLanguage.GERMAN -> "Befehl in gewählter Sprache nicht erkannt: \"$commandText\""
                        AppLanguage.TURKISH -> "Seçilen dilde komut tanınmadı: \"$commandText\""
                        else -> "Command not recognized in selected language: \"$commandText\""
                    }
                    Toast.makeText(context, unmatchedMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    try {
        speechRecognizer.startListening(intent)
    } catch (e: Exception) {
        viewModel.setListening(false)
        Toast.makeText(context, "Failed to start voice recognition", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Stateless Composable rendering an immersive full-screen Drive Mode interface
 * optimized with massive touch targets, gesture controls, and high-contrast typography.
 */
@Composable
fun DriveModeScreen(
    uiState: DriveModeUiState,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onVoiceAssistantClick: () -> Unit,
) {
    val context = LocalContext.current
    val song = uiState.currentSong
    val isPlaying = uiState.isPlaying

    // Extract album artwork bitmap off the main thread
    val artBitmap by produceState<Bitmap?>(initialValue = null, key1 = song?.uri) {
        val songUri = song?.uri
        value = if (songUri != null) {
            withContext(Dispatchers.IO) {
                songUri.toUri().extractAlbumArt(context)
            }
        } else null
    }

    // Gesture control animation states
    val density = LocalDensity.current
    val screenWidthPx = with(density) { 320.dp.toPx() }
    var dragOffset by remember { mutableStateOf(0f) }
    val animatedOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DriveModeGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header Bar with Exit/Back Button, Title, and Favorite Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = NeonBlueAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.drive_mode),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = NeonBlueAccent,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // 2. Middle Section: Album Art & Essential High-Contrast Metadata with Gesture Support
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive Album Art supporting tap to play/pause and swipe to skip
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer { translationX = animatedOffset.value }
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.5.dp, NeonBlueAccent, RoundedCornerShape(24.dp))
                        .background(Color(0xFF1B1238))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        if (dragOffset < -80f) {
                                            animatedOffset.animateTo(-screenWidthPx, animationSpec = tween(180))
                                            onSkipNextClick()
                                            animatedOffset.snapTo(screenWidthPx)
                                            animatedOffset.animateTo(0f, animationSpec = tween(180))
                                        } else if (dragOffset > 80f) {
                                            animatedOffset.animateTo(screenWidthPx, animationSpec = tween(180))
                                            onSkipPreviousClick()
                                            animatedOffset.snapTo(-screenWidthPx)
                                            animatedOffset.animateTo(0f, animationSpec = tween(180))
                                        } else {
                                            animatedOffset.animateTo(0f, animationSpec = spring())
                                        }
                                        dragOffset = 0f
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                    scope.launch {
                                        animatedOffset.snapTo(animatedOffset.value + dragAmount)
                                    }
                                }
                            )
                        }
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (artBitmap != null) {
                        Image(
                            bitmap = artBitmap!!.asImageBitmap(),
                            contentDescription = song?.title ?: "Album Artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = NeonBlueAccent.copy(alpha = 0.8f),
                                modifier = Modifier.size(96.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // High-contrast track title and artist name
                Text(
                    text = song?.title ?: stringResource(R.string.unknown_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = song?.artist ?: stringResource(R.string.unknown_artist),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonBlueAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.tap_or_swipe_hint),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
            }

            // 3. Bottom Section: Massive Touch Controls for Driving Safety
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Assistant Button (64dp target)
                    val isListening = uiState.isListening
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .border(
                                1.5.dp,
                                if (isListening) NeonBlueAccent else Color.White.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .clickable { onVoiceAssistantClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(R.string.voice_assistant),
                            tint = if (isListening) NeonBlueAccent else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Skip Previous Target (76dp target)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C1454))
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clickable { onSkipPreviousClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = stringResource(R.string.previous_track),
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Primary Play / Pause Target (100dp massive target with glow)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        NeonBlueAccent,
                                        Color(0xFF4A90E2)
                                    )
                                )
                            )
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { onPlayPauseClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                            tint = Color(0xFF0F0B21),
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    // Skip Next Target (76dp target)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C1454))
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .clickable { onSkipNextClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.next_track),
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }
        }
    }
}
