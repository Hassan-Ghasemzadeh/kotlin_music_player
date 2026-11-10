package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EqualizerUseCaseTest {

    private lateinit var fakeRepository: FakeEqualizerRepository
    private lateinit var getUseCase: GetEqualizerSettingsUseCase
    private lateinit var saveUseCase: SaveEqualizerSettingsUseCase
    private lateinit var applyUseCase: ApplyEqualizerUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeEqualizerRepository()
        getUseCase = GetEqualizerSettingsUseCase(fakeRepository)
        saveUseCase = SaveEqualizerSettingsUseCase(fakeRepository)
        applyUseCase = ApplyEqualizerUseCase(fakeRepository)
    }

    @Test
    fun getEqualizerSettings_returnsInitialState() {
        runTest {
            val state = getUseCase().first()
            assertEquals(false, state.isEnabled)
            assertEquals(0.toShort(), state.currentPresetId)
        }
    }

    @Test
    fun setEnabled_updatesRepositoryState() {
        runTest {
            saveUseCase.setEnabled(true)
            val state = getUseCase().first()
            assertTrue(state.isEnabled)
        }
    }

    @Test
    fun setPreset_updatesActivePreset() {
        runTest {
            val rockPreset = EqualizerPreset.DEFAULT_PRESETS.first { it.name == "Rock" }
            saveUseCase.setPreset(rockPreset)
            val state = getUseCase().first()
            assertEquals(rockPreset.id, state.currentPresetId)
            assertEquals("Rock", state.currentPresetName)
        }
    }

    @Test
    fun setBandLevel_switchesToCustomPreset() {
        runTest {
            saveUseCase.setBandLevel(0, 300)
            val state = getUseCase().first()
            assertEquals(EqualizerPreset.CUSTOM_PRESET_ID, state.currentPresetId)
            assertEquals(EqualizerPreset.CUSTOM_PRESET_NAME, state.currentPresetName)
            val band0 = state.bands.first { it.index == 0.toShort() }
            assertEquals(300.toShort(), band0.currentLevelMb)
        }
    }

    @Test
    fun setBassBoostAndVirtualizer_updatesStrengths() {
        runTest {
            saveUseCase.setBassBoost(600)
            saveUseCase.setVirtualizer(800)
            val state = getUseCase().first()
            assertEquals(600.toShort(), state.bassBoostStrength)
            assertEquals(800.toShort(), state.virtualizerStrength)
        }
    }

    @Test
    fun applyEqualizer_deliversStateToEngine() {
        runTest {
            saveUseCase.setEnabled(true)
            var appliedSessionId = -1
            var appliedState: EqualizerState? = null

            val engine = AudioEffectEngine { sessionId, state ->
                appliedSessionId = sessionId
                appliedState = state
            }

            applyUseCase(101, engine)
            assertEquals(101, appliedSessionId)
            assertTrue(appliedState?.isEnabled == true)
        }
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
