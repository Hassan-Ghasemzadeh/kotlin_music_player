package com.sftwr.cleandevelopment.musicplayer.music_player

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.sftwr.cleandevelopment.musicplayer.music_player.navigation.AppRootContent
import com.sftwr.cleandevelopment.musicplayer.music_player.ui.theme.Music_playerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point activity annotated with @AndroidEntryPoint
 * to enable Hilt dependency injection across Compose UI components and ViewModels.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable system window fitting to enable edge-to-edge layout design
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Apply application custom theme
            Music_playerTheme {
                // Initialize top-level navigation controller for Compose navigation graph
                val navController = rememberNavController()

                // Render root content container holding the navigation host and global UI elements
                AppRootContent(navController = navController)
            }
        }
    }
}