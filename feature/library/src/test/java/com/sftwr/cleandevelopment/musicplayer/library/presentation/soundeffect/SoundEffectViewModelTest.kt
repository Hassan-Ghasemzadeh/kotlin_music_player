package com.sftwr.cleandevelopment.musicplayer.library.presentation.soundeffect

import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffect
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SoundEffectRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSoundEffectsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SaveSoundEffectConfigUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController
import com.sftwr.cleandevelopment.musicplayer.library.core.soundeffect.SoundEffectAudioFxManager
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SoundEffectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSoundEffectRepository
    private lateinit var getUseCase: GetSoundEffectsUseCase
    private lateinit var saveUseCase: SaveSoundEffectConfigUseCase
    private lateinit var fakePlayerController: FakePlayerController
    private lateinit var fakeAdManager: FakeAdManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSoundEffectRepository()
        getUseCase = GetSoundEffectsUseCase(fakeRepository)
        saveUseCase = SaveSoundEffectConfigUseCase(fakeRepository)
        fakePlayerController = FakePlayerController()
        fakeAdManager = FakeAdManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleEnabled_updatesState() = runTest {
        val fxManager = SoundEffectAudioFxManager(getUseCase)
        val viewModel = SoundEffectViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        val collectJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEnabled)

        viewModel.toggleEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEnabled)

        collectJob.cancel()
    }

    @Test
    fun selectEffect_and_unlockEffect_updatesState() = runTest {
        val fxManager = SoundEffectAudioFxManager(getUseCase)
        val viewModel = SoundEffectViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        val collectJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.unlockEffect(SoundEffect.THREE_D_ROTATE)
        viewModel.selectEffect(SoundEffect.THREE_D_ROTATE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SoundEffect.THREE_D_ROTATE, viewModel.uiState.value.currentEffect)
        assertEquals(SoundEffect.THREE_D_ROTATE.id, viewModel.uiState.value.unlockedEffectId)

        collectJob.cancel()
    }

    @Test
    fun setVolumeOrBoost_updatesStateAndClamps() = runTest {
        val fxManager = SoundEffectAudioFxManager(getUseCase)
        val viewModel = SoundEffectViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        val collectJob = launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setVolumeOrBoost(0.85f)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0.85f, viewModel.uiState.value.volumeOrBoost, 0.001f)

        collectJob.cancel()
    }

    @Test
    fun pauseAndResumePlayback_callsExplicitPauseAndPlay() = runTest {
        val fxManager = SoundEffectAudioFxManager(getUseCase)
        val viewModel = SoundEffectViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        fakePlayerController.setIsPlaying(true)
        val wasPlaying = viewModel.pausePlaybackIfPlaying()

        assertTrue(wasPlaying)
        assertFalse(fakePlayerController.isPlaying.value)

        viewModel.resumePlaybackIfWasPlaying(wasPlaying)
        assertTrue(fakePlayerController.isPlaying.value)
    }

    private class FakeSoundEffectRepository : SoundEffectRepository {
        private val stateFlow = MutableStateFlow(SoundEffectState())

        override fun getSoundEffectState(): Flow<SoundEffectState> = stateFlow

        override suspend fun saveSoundEffectState(state: SoundEffectState) {
            stateFlow.value = state.copy(volumeOrBoost = state.volumeOrBoost.coerceIn(0f, 1f))
        }

        override suspend fun setEnabled(enabled: Boolean) {
            stateFlow.value = stateFlow.value.copy(isEnabled = enabled)
        }

        override suspend fun setCurrentEffect(effect: SoundEffect) {
            stateFlow.value = stateFlow.value.copy(currentEffect = effect)
        }

        override suspend fun setVolumeOrBoost(value: Float) {
            stateFlow.value = stateFlow.value.copy(volumeOrBoost = value.coerceIn(0f, 1f))
        }

        override suspend fun unlockEffect(effectId: String) {
            stateFlow.value = stateFlow.value.copy(unlockedEffectId = effectId)
        }

        override suspend fun lockEffects() {
            stateFlow.value = stateFlow.value.copy(unlockedEffectId = null)
        }
    }

    private class FakePlayerController : PlayerController {
        private val _isPlaying = MutableStateFlow(false)
        override val isPlaying: StateFlow<Boolean> get() = _isPlaying.asStateFlow()
        override val currentSong: StateFlow<SongUiModel?> get() = MutableStateFlow(null)

        fun setIsPlaying(playing: Boolean) {
            _isPlaying.value = playing
        }

        override fun togglePlayPause() {
            _isPlaying.value = !_isPlaying.value
        }

        override fun play() {
            _isPlaying.value = true
        }

        override fun pause() {
            _isPlaying.value = false
        }

        override fun playNext() {}
        override fun playPrevious() {}
    }

    private class FakeAdManager : AdManager {
        override val isAdFree: StateFlow<Boolean> = MutableStateFlow(false)
        override val isInitialized: StateFlow<Boolean> = MutableStateFlow(true)
        override fun initialize() {}
        override fun setUserConsent(activity: android.app.Activity, userConsent: Boolean) {}
        override fun setAdFree(isAdFree: Boolean) {}
        override fun requestBannerAd(
            zoneId: String,
            activity: android.app.Activity?,
            onSuccess: (adId: String) -> Unit,
            onFailure: (error: String) -> Unit
        ) {}
        override fun requestNativeAd(
            zoneId: String,
            activity: android.app.Activity?,
            onSuccess: (adId: String) -> Unit,
            onFailure: (error: String) -> Unit
        ) {}
        override fun requestRewardedAd(
            zoneId: String,
            activity: android.app.Activity?,
            onSuccess: (adId: String) -> Unit,
            onFailure: (error: String) -> Unit
        ) {}
        override fun showRewardedAd(
            adId: String,
            activity: android.app.Activity,
            onRewarded: () -> Unit,
            onError: (error: String) -> Unit,
            onClosed: () -> Unit
        ) {}
        override fun destroyBannerAd(zoneId: String) {}
        override fun destroyNativeAd(zoneId: String) {}
    }
}
