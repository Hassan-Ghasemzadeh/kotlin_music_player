package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing application metadata and developer details.
 *
 * @property appName Name of the application.
 * @property versionName Current version string of the app (e.g., "v3.0.4").
 * @property developerName Name of the developer or team.
 * @property githubUrl GitHub repository or profile URL.
 */
data class AppInfo(
    val appName: String = "Music Player",
    val versionName: String = "v3.0.4",
    val developerName: String = "Clean Development Studio",
    val githubUrl: String = "https://github.com"
)
