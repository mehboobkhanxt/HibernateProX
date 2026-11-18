package com.hibernateprox.app.core.engine

import com.hibernateprox.app.core.model.ForegroundTransition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundAppTracker @Inject constructor() {

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _currentForegroundPackage = MutableStateFlow<String?>(null)
    val currentForegroundPackage: StateFlow<String?> = _currentForegroundPackage.asStateFlow()

    private val _recentTransitions = MutableStateFlow<List<ForegroundTransition>>(emptyList())
    val recentTransitions: StateFlow<List<ForegroundTransition>> = _recentTransitions.asStateFlow()

    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
        if (!connected) {
            _currentForegroundPackage.value = null
        }
    }

    fun onForegroundWindowChanged(packageName: String, timestampMs: Long = System.currentTimeMillis()) {
        if (packageName.isBlank()) return
        val previous = _currentForegroundPackage.value
        _currentForegroundPackage.value = packageName
        if (previous != packageName) {
            _recentTransitions.update { existing ->
                (listOf(ForegroundTransition(packageName, timestampMs)) + existing)
                    .take(MAX_HISTORY)
            }
        }
    }

    companion object {
        private const val MAX_HISTORY = 15
    }
}
