package com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer

import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppSettings
import com.sftwr.cleandevelopment.musicplayer.domain.model.ColorPalette
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SettingsRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetEqualizerSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SaveEqualizerSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.equalizer.EqualizerAudioFxManager
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EqualizerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeEqualizerRepository
    private lateinit var getUseCase: GetEqualizerSettingsUseCase
    private lateinit var saveUseCase: SaveEqualizerSettingsUseCase
    private lateinit var fakePlayerController: FakePlayerController
    private lateinit var fakeAdManager: FakeAdManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeEqualizerRepository()
        getUseCase = GetEqualizerSettingsUseCase(fakeRepository)
        saveUseCase = SaveEqualizerSettingsUseCase(fakeRepository, FakeSettingsRepository())
        fakePlayerController = FakePlayerController()
        fakeAdManager = FakeAdManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_reflectsRepositoryUpdates() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun selectPreset_updatesPresetInUiState() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val rockPreset = EqualizerPreset.DEFAULT_PRESETS.first { it.displayName == "Rock" }
        viewModel.selectPreset(rockPreset)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(rockPreset.id, viewModel.uiState.value.currentPresetId)
        assertEquals("Rock", viewModel.uiState.value.currentPresetName)
    }

    @Test
    fun setBandLevel_updatesBandAndPresetToCustom() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setBandLevel(0, 500)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(EqualizerPreset.CUSTOM_PRESET_ID, viewModel.uiState.value.currentPresetId)
        assertEquals(EqualizerPreset.CUSTOM_PRESET_NAME, viewModel.uiState.value.currentPresetName)
        assertEquals(500.toShort(), viewModel.uiState.value.bands.first { it.index == 0.toShort() }.currentLevelMb)
    }

    @Test
    fun setBassBoostAndVirtualizer_updatesStateValues() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setBassBoostStrength(750)
        viewModel.setVirtualizerStrength(900)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(750.toShort(), viewModel.uiState.value.bassBoostStrength)
        assertEquals(900.toShort(), viewModel.uiState.value.virtualizerStrength)
    }

    @Test
    fun unlockAndSelectPreset_unlocksAndSelectsPreset() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager, fakePlayerController, fakeAdManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val rockPreset = EqualizerPreset.DEFAULT_PRESETS.first { it.displayName == "Rock" }
        viewModel.unlockAndSelectPreset(rockPreset)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(rockPreset.id, viewModel.uiState.value.unlockedPresetId)
        assertEquals(rockPreset.id, viewModel.uiState.value.currentPresetId)
    }
}

private class FakePlayerController : PlayerController {
    override val currentSong: StateFlow<SongUiModel?> = MutableStateFlow(null)
    override val isPlaying: StateFlow<Boolean> = MutableStateFlow(false)
    override fun togglePlayPause() {}
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

private class FakeEqualizerRepository : EqualizerRepository {
    private val stateFlow = MutableStateFlow(EqualizerState())

    override fun getEqualizerState(): Flow<EqualizerState> = stateFlow

    override suspend fun saveEqualizerState(state: EqualizerState) {
        stateFlow.value = state
    }

    override suspend fun setEnabled(enabled: Boolean) {
        stateFlow.value = stateFlow.value.copy(isEnabled = enabled)
    }

    override suspend fun setPreset(preset: EqualizerPreset) {
        val updatedBands = stateFlow.value.bands.map { band ->
            val level = preset.bandLevels[band.index] ?: band.currentLevelMb
            band.copy(currentLevelMb = level)
        }
        stateFlow.value = stateFlow.value.copy(
            currentPresetId = preset.id,
            currentPresetName = preset.displayName,
            bands = updatedBands
        )
    }

    override suspend fun setBandLevel(bandIndex: Short, levelMb: Short) {
        val updatedBands = stateFlow.value.bands.map { band ->
            if (band.index == bandIndex) band.copy(currentLevelMb = levelMb) else band
        }
        stateFlow.value = stateFlow.value.copy(
            currentPresetId = EqualizerPreset.CUSTOM_PRESET_ID,
            currentPresetName = EqualizerPreset.CUSTOM_PRESET_NAME,
            bands = updatedBands
        )
    }

    override suspend fun setBassBoostStrength(strength: Short) {
        stateFlow.value = stateFlow.value.copy(bassBoostStrength = strength)
    }

    override suspend fun setVirtualizerStrength(strength: Short) {
        stateFlow.value = stateFlow.value.copy(virtualizerStrength = strength)
    }

    override suspend fun unlockPreset(presetId: Short) {
        stateFlow.value = stateFlow.value.copy(unlockedPresetId = presetId)
    }

    override suspend fun lockPresets() {
        stateFlow.value = stateFlow.value.copy(unlockedPresetId = null)
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val settingsFlow = MutableStateFlow(AppSettings())
    override fun getSettings(): Flow<AppSettings> = settingsFlow
    override suspend fun updateLanguage(language: AppLanguage) {}
    override suspend fun updateColorPalette(palette: ColorPalette) {}
    override suspend fun updateGlassmorphicStyle(enabled: Boolean) {}
    override suspend fun updateAlbumArtworkEnabled(enabled: Boolean) {}
    override suspend fun updateShowAlbumArt(enabled: Boolean) {}
    override suspend fun updatePlaybackSpeed(speed: Float) {}
    override suspend fun updatePlaybackPitch(pitch: Float) {}
    override suspend fun updateCrossfadeDuration(seconds: Int) {}
    override suspend fun updateSkipSilence(enabled: Boolean) {}
    override suspend fun updateDefaultEqualizerPreset(preset: EqualizerPreset) {
        settingsFlow.value = settingsFlow.value.copy(defaultEqualizerPreset = preset)
    }
    override suspend fun updateDefaultEqualizerPreset(preset: String) {
        updateDefaultEqualizerPreset(EqualizerPreset.fromName(preset))
    }
    override suspend fun updateCircularVisualizerType(typeKey: String) {}
    override suspend fun updateReplayGainMode(mode: ReplayGainMode) {}
    override suspend fun addExcludedFolder(path: String) {}
    override suspend fun removeExcludedFolder(path: String) {}
    override suspend fun updateMinTrackDuration(seconds: Int) {}
    override suspend fun updateLockscreenControls(enabled: Boolean) {}
    override suspend fun updateScreenOffPlayback(enabled: Boolean) {}
    override suspend fun updateHeadsetControls(enabled: Boolean) {}
    override suspend fun updateAllowExternalDevicePlayback(enabled: Boolean) {}
    override suspend fun updateShakeToSkip(enabled: Boolean) {}
    override suspend fun updateSwipeArtworkToSkip(enabled: Boolean) {}
}
