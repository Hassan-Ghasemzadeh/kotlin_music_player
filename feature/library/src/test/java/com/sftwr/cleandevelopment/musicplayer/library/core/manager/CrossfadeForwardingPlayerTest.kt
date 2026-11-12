package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class CrossfadeForwardingPlayerTest {

    private fun createFakeExoPlayer(listenersSet: MutableSet<Player.Listener>): ExoPlayer {
        var self: ExoPlayer? = null
        self = Proxy.newProxyInstance(
            ExoPlayer::class.java.classLoader,
            arrayOf(ExoPlayer::class.java)
        ) { _, method, args ->
            when (method.name) {
                "addListener" -> {
                    listenersSet.add(args[0] as Player.Listener)
                    null
                }
                "removeListener" -> {
                    listenersSet.remove(args[0] as Player.Listener)
                    null
                }
                "isPlaying" -> false
                "getDuration" -> 100000L
                "equals" -> self === args[0]
                "hashCode" -> System.identityHashCode(self)
                "toString" -> "FakeExoPlayer"
                else -> {
                    when (method.returnType) {
                        Boolean::class.javaPrimitiveType -> false
                        Int::class.javaPrimitiveType -> 0
                        Long::class.javaPrimitiveType -> 0L
                        Float::class.javaPrimitiveType -> 0f
                        else -> null
                    }
                }
            }
        } as ExoPlayer
        return self
    }

    @Test
    fun `switching delegate transfers listeners and updates active player`() {
        val listenersA = mutableSetOf<Player.Listener>()
        val listenersB = mutableSetOf<Player.Listener>()
        val playerA = createFakeExoPlayer(listenersA)
        val playerB = createFakeExoPlayer(listenersB)

        val forwardingPlayer = CrossfadeForwardingPlayer(playerA)

        val testListener = object : Player.Listener {}
        forwardingPlayer.addListener(testListener)

        assertTrue(listenersA.contains(testListener))

        forwardingPlayer.setDelegate(playerB)

        assertEquals(playerB, forwardingPlayer.currentActivePlayer)
        assertFalse(listenersA.contains(testListener))
        assertTrue(listenersB.contains(testListener))
    }
}
