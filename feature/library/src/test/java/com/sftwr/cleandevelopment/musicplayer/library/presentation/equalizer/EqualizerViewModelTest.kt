package com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetEqualizerSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SaveEqualizerSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.equalizer.EqualizerAudioFxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeEqualizerRepository()
        getUseCase = GetEqualizerSettingsUseCase(fakeRepository)
        saveUseCase = SaveEqualizerSettingsUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_reflectsRepositoryUpdates() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun selectPreset_updatesPresetInUiState() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val rockPreset = EqualizerPreset.DEFAULT_PRESETS.first { it.name == "Rock" }
        viewModel.selectPreset(rockPreset)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(rockPreset.id, viewModel.uiState.value.currentPresetId)
        assertEquals("Rock", viewModel.uiState.value.currentPresetName)
    }

    @Test
    fun setBandLevel_updatesBandAndPresetToCustom() = runTest {
        val fxManager = EqualizerAudioFxManager(getUseCase)
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager)

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
        val viewModel = EqualizerViewModel(getUseCase, saveUseCase, fxManager)

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setBassBoostStrength(750)
        viewModel.setVirtualizerStrength(900)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(750.toShort(), viewModel.uiState.value.bassBoostStrength)
        assertEquals(900.toShort(), viewModel.uiState.value.virtualizerStrength)
    }
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
            currentPresetName = preset.name,
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
}
