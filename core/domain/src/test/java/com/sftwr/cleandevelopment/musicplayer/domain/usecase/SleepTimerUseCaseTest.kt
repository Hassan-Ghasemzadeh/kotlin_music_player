package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.SleepTimerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerUseCaseTest {

    private lateinit var fakeRepository: FakeSleepTimerRepository
    private lateinit var getRemainingTimeUseCase: GetSleepTimerRemainingTimeUseCase
    private lateinit var setSleepTimerUseCase: SetSleepTimerUseCase
    private lateinit var cancelSleepTimerUseCase: CancelSleepTimerUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeSleepTimerRepository()
        getRemainingTimeUseCase = GetSleepTimerRemainingTimeUseCase(fakeRepository)
        setSleepTimerUseCase = SetSleepTimerUseCase(fakeRepository)
        cancelSleepTimerUseCase = CancelSleepTimerUseCase(fakeRepository)
    }

    @Test
    fun getSleepTimerState_emitsInitialInactiveState() = runTest {
        val state = getRemainingTimeUseCase().first()
        assertFalse(state.isActive)
        assertEquals(0, state.durationMinutes)
    }

    @Test
    fun setSleepTimer_updatesStateToActive() = runTest {
        setSleepTimerUseCase(30)
        val state = getRemainingTimeUseCase().first()
        assertTrue(state.isActive)
        assertEquals(30, state.durationMinutes)
    }

    @Test
    fun setSleepTimer_withZero_cancelsTimer() = runTest {
        setSleepTimerUseCase(30)
        setSleepTimerUseCase(0)
        val state = getRemainingTimeUseCase().first()
        assertFalse(state.isActive)
    }

    @Test
    fun cancelSleepTimer_resetsState() = runTest {
        setSleepTimerUseCase(60)
        cancelSleepTimerUseCase()
        val state = getRemainingTimeUseCase().first()
        assertFalse(state.isActive)
        assertEquals(0, state.durationMinutes)
    }
}

class FakeSleepTimerRepository : SleepTimerRepository {
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
