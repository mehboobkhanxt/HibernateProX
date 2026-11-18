package com.hibernateprox.app.feature.profiles

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
class ProfilesViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    val selectedProfileKey: StateFlow<String> = prefsRepo.prefsFlow
        .map { it.selectedProfileKey }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = "battery",
        )

    fun selectProfile(key: String) {
        viewModelScope.launch {
            prefsRepo.setSelectedProfile(key)
        }
    }
}
