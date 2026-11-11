package com.sftwr.cleandevelopment.musicplayer.library.presentation.rate

import com.sftwr.cleandevelopment.musicplayer.domain.model.StoreOption

/**
 * UI State for the Rate Us bottom sheet.
 */
data class RateUsUiState(
    val stores: List<StoreOption> = emptyList(),
    val isLoading: Boolean = false
)
