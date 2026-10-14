package com.sftwr.cleandevelopment.musicplayer.music_player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sftwr.cleandevelopment.musicplayer.home.presentation.HomeScreen
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
        setContent {
            Music_playerTheme {
                val navController = rememberNavController()
                AppRootContent(navController = navController)
            }
        }
    }
}
