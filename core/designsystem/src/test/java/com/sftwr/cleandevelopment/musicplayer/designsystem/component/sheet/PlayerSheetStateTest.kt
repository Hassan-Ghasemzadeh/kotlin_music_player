package com.sftwr.cleandevelopment.musicplayer.designsystem.component.sheet

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private object ImmediateFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return onFrame(System.nanoTime())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerSheetStateTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_defaultsToCollapsed() {
        val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
        assertEquals(PlayerSheetValue.Collapsed, state.targetValue)
        assertEquals(0f, state.progress, 0.001f)
        assertTrue(state.isCollapsed)
        assertFalse(state.isExpanded)
    }

    @Test
    fun initialState_canBeExpanded() {
        val state = PlayerSheetState(initialValue = PlayerSheetValue.Expanded)
        assertEquals(PlayerSheetValue.Expanded, state.targetValue)
        assertEquals(1f, state.progress, 0.001f)
        assertTrue(state.isExpanded)
        assertFalse(state.isCollapsed)
    }

    @Test
    fun expand_animatesToExpandedState() = runBlocking {
        withContext(ImmediateFrameClock) {
            val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
            state.expand()
            assertEquals(PlayerSheetValue.Expanded, state.targetValue)
            assertEquals(1f, state.progress, 0.001f)
            assertTrue(state.isExpanded)
        }
    }

    @Test
    fun collapse_animatesToCollapsedState() = runBlocking {
        withContext(ImmediateFrameClock) {
            val state = PlayerSheetState(initialValue = PlayerSheetValue.Expanded)
            state.collapse()
            assertEquals(PlayerSheetValue.Collapsed, state.targetValue)
            assertEquals(0f, state.progress, 0.001f)
            assertTrue(state.isCollapsed)
        }
    }

    @Test
    fun toggle_switchesBetweenCollapsedAndExpanded() = runBlocking {
        withContext(ImmediateFrameClock) {
            val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
            state.toggle()
            assertEquals(PlayerSheetValue.Expanded, state.targetValue)

            state.toggle()
            assertEquals(PlayerSheetValue.Collapsed, state.targetValue)
        }
    }

    @Test
    fun dispatchRawDelta_updatesProgressCorrectly() = runBlocking {
        val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
        val maxOffsetPx = 1000f

        // Swipe up by 500px (-500 delta) -> progress increases by 0.5
        state.dispatchRawDelta(deltaY = -500f, maxOffsetPx = maxOffsetPx)
        assertEquals(0.5f, state.progress, 0.001f)

        // Swipe up another 300px -> progress becomes 0.8
        state.dispatchRawDelta(deltaY = -300f, maxOffsetPx = maxOffsetPx)
        assertEquals(0.8f, state.progress, 0.001f)

        // Swipe down by 400px -> progress drops to 0.4
        state.dispatchRawDelta(deltaY = 400f, maxOffsetPx = maxOffsetPx)
        assertEquals(0.4f, state.progress, 0.001f)
    }

    @Test
    fun dispatchRawDelta_clampsProgressBetweenZeroAndOne() = runBlocking {
        val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
        val maxOffsetPx = 1000f

        // Overswipe up by 2000px
        state.dispatchRawDelta(deltaY = -2000f, maxOffsetPx = maxOffsetPx)
        assertEquals(1.0f, state.progress, 0.001f)

        // Overswipe down by 3000px
        state.dispatchRawDelta(deltaY = 3000f, maxOffsetPx = maxOffsetPx)
        assertEquals(0.0f, state.progress, 0.001f)
    }

    @Test
    fun settle_highUpwardVelocity_expandsSheet() = runBlocking {
        withContext(ImmediateFrameClock) {
            val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
            state.snapTo(0.1f)
            state.settle(velocityPxPerSec = -1000f)

            assertEquals(PlayerSheetValue.Expanded, state.targetValue)
        }
    }

    @Test
    fun settle_highDownwardVelocity_collapsesSheet() = runBlocking {
        withContext(ImmediateFrameClock) {
            val state = PlayerSheetState(initialValue = PlayerSheetValue.Expanded)
            state.snapTo(0.9f)
            state.settle(velocityPxPerSec = 1000f)

            assertEquals(PlayerSheetValue.Collapsed, state.targetValue)
        }
    }

    @Test
    fun settle_aboveThreshold_expandsSheet() = runBlocking {
        withContext(ImmediateFrameClock) {
            val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
            state.snapTo(0.5f)
            state.settle(velocityPxPerSec = 0f)

            assertEquals(PlayerSheetValue.Expanded, state.targetValue)
        }
    }

    @Test
    fun settle_belowThreshold_collapsesSheet() = runBlocking {
        withContext(ImmediateFrameClock) {
            val state = PlayerSheetState(initialValue = PlayerSheetValue.Collapsed)
            state.snapTo(0.2f)
            state.settle(velocityPxPerSec = 0f)

            assertEquals(PlayerSheetValue.Collapsed, state.targetValue)
        }
    }
}
