package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.ColorPalette
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateAppearanceSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemePickerUiState(
    val currentPalette: ColorPalette = ColorPalette.NEON_DARK,
    val language: AppLanguage = AppLanguage.ENGLISH
)

@HiltViewModel
class ThemePickerViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val updateAppearanceSettingsUseCase: UpdateAppearanceSettingsUseCase
) : ViewModel() {

    val uiState: StateFlow<ThemePickerUiState> = getSettingsUseCase()
        .map { settings ->
            ThemePickerUiState(
                currentPalette = settings.colorPalette,
                language = settings.language
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePickerUiState()
        )

    fun applyPalette(palette: ColorPalette, onApplied: () -> Unit) {
        viewModelScope.launch {
            updateAppearanceSettingsUseCase.setColorPalette(palette)
            onApplied()
        }
    }
}
