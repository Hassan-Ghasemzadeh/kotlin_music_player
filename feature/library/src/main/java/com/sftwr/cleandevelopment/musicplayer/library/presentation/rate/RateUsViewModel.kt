package com.sftwr.cleandevelopment.musicplayer.library.presentation.rate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetAvailableStoresUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.LaunchStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RateUsViewModel @Inject constructor(
    private val getAvailableStoresUseCase: GetAvailableStoresUseCase,
    private val launchStoreUseCase: LaunchStoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RateUsUiState())
    val uiState: StateFlow<RateUsUiState> = _uiState.asStateFlow()

    fun loadStores(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val stores = getAvailableStoresUseCase(context, context.packageName)
            _uiState.update {
                it.copy(
                    stores = stores,
                    isLoading = false
                )
            }
        }
    }

    fun selectStore(context: Context, storeId: String) {
        launchStoreUseCase(context, storeId, context.packageName)
    }
}
