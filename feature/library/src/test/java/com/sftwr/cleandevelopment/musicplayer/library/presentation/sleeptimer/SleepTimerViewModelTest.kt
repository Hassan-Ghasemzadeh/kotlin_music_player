package com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer

import com.sftwr.cleandevelopment.musicplayer.domain.model.SleepTimerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.CancelSleepTimerUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSleepTimerRemainingTimeUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SetSleepTimerUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSleepTimerRepositoryForViewModel
    private lateinit var getRemainingTimeUseCase: GetSleepTimerRemainingTimeUseCase
    private lateinit var setSleepTimerUseCase: SetSleepTimerUseCase
    private lateinit var cancelSleepTimerUseCase: CancelSleepTimerUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSleepTimerRepositoryForViewModel()
        getRemainingTimeUseCase = GetSleepTimerRemainingTimeUseCase(fakeRepository)
        setSleepTimerUseCase = SetSleepTimerUseCase(fakeRepository)
        cancelSleepTimerUseCase = CancelSleepTimerUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initialStateIsInactive() = runTest {
        val viewModel = SleepTimerViewModel(
            getRemainingTimeUseCase,
            setSleepTimerUseCase,
            cancelSleepTimerUseCase
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isActive)
        assertEquals(TimerPreset.OFF, viewModel.uiState.value.selectedPreset)
    }

    @Test
    fun selectPreset_30Minutes_updatesUiStateAndRepository() = runTest {
        val viewModel = SleepTimerViewModel(
            getRemainingTimeUseCase,
            setSleepTimerUseCase,
            cancelSleepTimerUseCase
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPreset(TimerPreset.MIN_30)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isActive)
        assertEquals(TimerPreset.MIN_30, viewModel.uiState.value.selectedPreset)
        assertEquals(30, viewModel.uiState.value.durationMinutes)
    }

    @Test
    fun selectPreset_Custom_opensTimePicker() = runTest {
        val viewModel = SleepTimerViewModel(
            getRemainingTimeUseCase,
            setSleepTimerUseCase,
            cancelSleepTimerUseCase
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPreset(TimerPreset.CUSTOM)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCustomTimePicker)
        assertEquals(TimerPreset.CUSTOM, viewModel.uiState.value.selectedPreset)
    }

    @Test
    fun setCustomTime_updatesUiStateAndRepository() = runTest {
        val viewModel = SleepTimerViewModel(
            getRemainingTimeUseCase,
            setSleepTimerUseCase,
            cancelSleepTimerUseCase
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setCustomTime(1, 45) // 1 hour 45 minutes = 105 minutes
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCustomTimePicker)
        assertTrue(viewModel.uiState.value.isActive)
        assertEquals(105, viewModel.uiState.value.durationMinutes)
        assertEquals(1, viewModel.uiState.value.customHours)
        assertEquals(45, viewModel.uiState.value.customMinutes)
    }

    @Test
    fun cancelTimer_resetsStateToOff() = runTest {
        val viewModel = SleepTimerViewModel(
            getRemainingTimeUseCase,
            setSleepTimerUseCase,
            cancelSleepTimerUseCase
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPreset(TimerPreset.HOUR_1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isActive)

        viewModel.cancelTimer()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isActive)
        assertEquals(TimerPreset.OFF, viewModel.uiState.value.selectedPreset)
    }
}

class FakeSleepTimerRepositoryForViewModel : SleepTimerRepository {
    private val _state = MutableStateFlow(SleepTimerState())

    override fun getSleepTimerState(): Flow<SleepTimerState> = _state

    override suspend fun setSleepTimer(durationMinutes: Int) {
        if (durationMinutes <= 0) {
            cancelSleepTimer()
        } else {
            val targetMs = System.currentTimeMillis() + durationMinutes * 60 * 1000L
            _state.value = SleepTimerState(
                isActive = true,
                targetEndTimeMs = targetMs,
                durationMinutes = durationMinutes,
                remainingTimeMs = durationMinutes * 60 * 1000L
            )
        }
    }

    override suspend fun cancelSleepTimer() {
        _state.value = SleepTimerState()
    }

    override suspend fun getRemainingTimeMs(): Long {
        return _state.value.remainingTimeMs
    }
}
