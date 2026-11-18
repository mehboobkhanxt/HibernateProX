package com.hibernateprox.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hibernateprox.app.core.model.StopMode
import com.hibernateprox.app.core.model.SystemTelemetrySnapshot
import com.hibernateprox.app.core.telemetry.DeviceMetricsReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val telemetry: SystemTelemetrySnapshot = SystemTelemetrySnapshot(),
    val activeMode: StopMode = StopMode.UNAVAILABLE,
    val isPreviewPulsing: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceMetricsReader: DeviceMetricsReader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshMetrics()
    }

    fun refreshMetrics() {
        viewModelScope.launch {
            val snapshot = deviceMetricsReader.readSnapshot()
            _uiState.update { it.copy(telemetry = snapshot) }
        }
    }

    fun triggerPulsePreview() {
        if (_uiState.value.isPreviewPulsing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPreviewPulsing = true) }
            delay(1400L)
            val refreshed = deviceMetricsReader.readSnapshot()
            _uiState.update { it.copy(isPreviewPulsing = false, telemetry = refreshed) }
        }
    }
}
