package com.sftwr.cleandevelopment.musicplayer.music_player

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import com.sftwr.cleandevelopment.musicplayer.music_player.navigation.AppRootContent
import com.sftwr.cleandevelopment.musicplayer.music_player.ui.theme.Music_playerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main entry point activity annotated with @AndroidEntryPoint
 * to enable Hilt dependency injection across Compose UI components and ViewModels.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adManager: AdManager

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Tapsell SDK and pass mandatory Activity User Consent (GDPR compliance)
        adManager.initialize()
        adManager.setUserConsent(this, userConsent = true)

        // Enable edge-to-edge display and disable system window fitting
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.isNavigationBarContrastEnforced = false

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
