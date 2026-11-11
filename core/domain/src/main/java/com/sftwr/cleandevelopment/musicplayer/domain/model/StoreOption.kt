package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing an app store option available for rating.
 *
 * @property id Unique store identifier (e.g., "bazaar", "myket", "google_play").
 * @property name Display name of the store (e.g., "Cafe Bazaar", "Myket").
 * @property packageName Android package name of the store app.
 * @property marketUri Market URI scheme for direct app store opening.
 * @property webUrl Fallback web URL for the store app page.
 */
data class StoreOption(
    val id: String,
    val name: String,
    val packageName: String,
    val marketUri: String,
    val webUrl: String
)
