package com.hibernateprox.app.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hibernateprox.app.core.data.AppRulesRepository
import com.hibernateprox.app.core.engine.AccessibilityForceStopCoordinator
import com.hibernateprox.app.core.engine.AccessibilityHelper
import com.hibernateprox.app.core.engine.ForegroundAppTracker
import com.hibernateprox.app.core.model.AppRuleMode
import com.hibernateprox.app.core.model.ForceStopOutcome
import com.hibernateprox.app.core.model.ForceStopState
import com.hibernateprox.app.core.model.ForegroundTransition
import com.hibernateprox.app.core.model.StopMode
import com.hibernateprox.app.core.model.SystemTelemetrySnapshot
import com.hibernateprox.app.core.telemetry.DeviceMetricsReader
import com.hibernateprox.app.core.telemetry.InstalledAppsScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatchSummary(
    val stoppedCount: Int,
    val alreadyIdleCount: Int,
    val failedCount: Int,
)

data class DashboardUiState(
    val telemetry: SystemTelemetrySnapshot = SystemTelemetrySnapshot(),
    val activeMode: StopMode = StopMode.UNAVAILABLE,
    val isAccessibilityEnabled: Boolean = false,
    val currentForegroundPackage: String? = null,
    val recentTransitions: List<ForegroundTransition> = emptyList(),
    val activeTargetCount: Int = 0,
    val isBatchRunning: Boolean = false,
    val showDisclosureDialog: Boolean = false,
    val stopState: ForceStopState = ForceStopState.Idle,
    val lastBatchSummary: BatchSummary? = null,
    val lastRunTimestampMs: Long? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceMetricsReader: DeviceMetricsReader,
    private val scanner: InstalledAppsScanner,
    private val foregroundAppTracker: ForegroundAppTracker,
    private val coordinator: AccessibilityForceStopCoordinator,
    private val appRulesRepository: AppRulesRepository,
) : ViewModel() {

    private val telemetry = MutableStateFlow(SystemTelemetrySnapshot())
    private val isAccessibilityEnabled = MutableStateFlow(false)
    private val activeTargetCount = MutableStateFlow(0)
    private val isBatchRunning = MutableStateFlow(false)
    private val showDisclosureDialog = MutableStateFlow(false)
    private val lastBatchSummary = MutableStateFlow<BatchSummary?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        telemetry,
        isAccessibilityEnabled,
        foregroundAppTracker.currentForegroundPackage,
        foregroundAppTracker.recentTransitions,
        activeTargetCount,
        isBatchRunning,
        showDisclosureDialog,
        coordinator.state,
        lastBatchSummary,
        appRulesRepository.observeLastRunTimestamp(),
    ) { values ->
        val a11yEnabled = values[1] as Boolean
        @Suppress("UNCHECKED_CAST")
        val transitions = values[3] as List<ForegroundTransition>
        DashboardUiState(
            telemetry = values[0] as SystemTelemetrySnapshot,
            activeMode = if (a11yEnabled) StopMode.ACCESSIBILITY else StopMode.UNAVAILABLE,
            isAccessibilityEnabled = a11yEnabled,
            currentForegroundPackage = values[2] as String?,
            recentTransitions = transitions,
            activeTargetCount = values[4] as Int,
            isBatchRunning = values[5] as Boolean,
            showDisclosureDialog = values[6] as Boolean,
            stopState = values[7] as ForceStopState,
            lastBatchSummary = values[8] as BatchSummary?,
            lastRunTimestampMs = values[9] as Long?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DashboardUiState(),
    )

    init {
        refreshMetrics()
    }

    fun refreshMetrics() {
        viewModelScope.launch {
            appRulesRepository.ensureDefaultProfilesSeeded()
            isAccessibilityEnabled.value = AccessibilityHelper.isAccessibilityServiceEnabled(context)
            telemetry.value = deviceMetricsReader.readSnapshot()
            val ruleMap = appRulesRepository.observeRuleMap().first()
            val apps = scanner.scanInstalledApps(foregroundAppTracker.currentForegroundPackage.value)
            activeTargetCount.value = apps.count { app ->
                val rule = ruleMap[app.packageName] ?: AppRuleMode.DEFAULT
                !app.isProtectedSafeList &&
                    !app.isStopped &&
                    rule != AppRuleMode.WHITELIST &&
                    (!app.isSystemApp || rule == AppRuleMode.BLACKLIST)
            }
        }
    }

    fun onHibernateAllClick() {
        if (isBatchRunning.value) return
        if (!AccessibilityHelper.isAccessibilityServiceEnabled(context)) {
            showDisclosureDialog.value = true
            return
        }
        viewModelScope.launch {
            isBatchRunning.value = true
            val ruleMap = appRulesRepository.observeRuleMap().first()
            val targets = scanner.scanInstalledApps(
                foregroundAppTracker.currentForegroundPackage.value,
            ).filter { app ->
                val rule = ruleMap[app.packageName] ?: AppRuleMode.DEFAULT
                !app.isProtectedSafeList &&
                    !app.isStopped &&
                    rule != AppRuleMode.WHITELIST &&
                    (!app.isSystemApp || rule == AppRuleMode.BLACKLIST)
            }

            var stopped = 0
            var idle = 0
            var failed = 0
            for (target in targets) {
                val outcome = coordinator.requestForceStop(target.packageName, target.label)
                appRulesRepository.recordHibernationLog(
                    packageName = target.packageName,
                    appLabel = target.label,
                    outcome = outcome,
                    modeUsed = StopMode.ACCESSIBILITY,
                )
                when (outcome) {
                    ForceStopOutcome.VERIFIED_STOPPED -> stopped++
                    ForceStopOutcome.ALREADY_STOPPED -> idle++
                    else -> failed++
                }
            }
            lastBatchSummary.value = BatchSummary(stopped, idle, failed)
            isBatchRunning.value = false
            refreshMetrics()
        }
    }

    fun openDisclosure() {
        showDisclosureDialog.value = true
    }

    fun dismissDisclosure() {
        showDisclosureDialog.value = false
    }

    fun openAccessibilitySettings() {
        showDisclosureDialog.value = false
        runCatching {
            context.startActivity(AccessibilityHelper.createAccessibilitySettingsIntent())
        }
    }
}
