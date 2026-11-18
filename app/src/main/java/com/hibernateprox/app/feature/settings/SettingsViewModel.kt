package com.hibernateprox.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hibernateprox.app.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    val useDynamicColor: StateFlow<Boolean> = prefsRepo.prefsFlow
        .map { it.useDynamicColor }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = false,
        )

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setDynamicColor(enabled)
        }
    }
}
