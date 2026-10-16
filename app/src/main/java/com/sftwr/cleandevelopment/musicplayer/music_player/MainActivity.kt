package com.sftwr.cleandevelopment.musicplayer.music_player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.sftwr.cleandevelopment.musicplayer.music_player.navigation.AppRootContent
import com.sftwr.cleandevelopment.musicplayer.music_player.ui.theme.Music_playerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point activity annotated with @AndroidEntryPoint
 * to enable Hilt dependency injection for Compose and ViewModels.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        setContent {
            Music_playerTheme {
                val navController = rememberNavController()
                AppRootContent(navController = navController)
            }
        }
    }
}
