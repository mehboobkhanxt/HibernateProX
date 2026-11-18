package com.hibernateprox.app.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hibernateprox.app.core.data.AppRulesRepository
import com.hibernateprox.app.core.datastore.UserPreferencesRepository
import com.hibernateprox.app.core.engine.AccessibilityForceStopCoordinator
import com.hibernateprox.app.core.engine.AccessibilityHelper
import com.hibernateprox.app.core.engine.BatchAppOutcome
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
    val items: List<BatchAppOutcome> = emptyList(),
)

data class DashboardUiState(
    val telemetry: SystemTelemetrySnapshot = SystemTelemetrySnapshot(),
    val activeMode: StopMode = StopMode.UNAVAILABLE,
    val isAccessibilityEnabled: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val currentForegroundPackage: String? = null,
    val recentTransitions: List<ForegroundTransition> = emptyList(),
    val activeTargetCount: Int = 0,
    val isBatchRunning: Boolean = false,
    val showDisclosureDialog: Boolean = false,
    val showOverlayDialog: Boolean = false,
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
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val telemetry = MutableStateFlow(SystemTelemetrySnapshot())
    private val isAccessibilityEnabled = MutableStateFlow(false)
    private val canDrawOverlays = MutableStateFlow(false)
    private val activeTargetCount = MutableStateFlow(0)
    private val isBatchRunning = MutableStateFlow(false)
    private val showDisclosureDialog = MutableStateFlow(false)
    private val showOverlayDialog = MutableStateFlow(false)
    private val lastBatchSummary = MutableStateFlow<BatchSummary?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        telemetry,
        isAccessibilityEnabled,
        canDrawOverlays,
        foregroundAppTracker.currentForegroundPackage,
        foregroundAppTracker.recentTransitions,
        activeTargetCount,
        isBatchRunning,
        showDisclosureDialog,
        showOverlayDialog,
        coordinator.state,
        lastBatchSummary,
        appRulesRepository.observeLastRunTimestamp(),
    ) { values ->
        val a11yEnabled = values[1] as Boolean
        @Suppress("UNCHECKED_CAST")
        val transitions = values[4] as List<ForegroundTransition>
        DashboardUiState(
            telemetry = values[0] as SystemTelemetrySnapshot,
            activeMode = if (a11yEnabled) StopMode.ACCESSIBILITY else StopMode.UNAVAILABLE,
            isAccessibilityEnabled = a11yEnabled,
            canDrawOverlays = values[2] as Boolean,
            currentForegroundPackage = values[3] as String?,
            recentTransitions = transitions,
            activeTargetCount = values[5] as Int,
            isBatchRunning = values[6] as Boolean,
            showDisclosureDialog = values[7] as Boolean,
            showOverlayDialog = values[8] as Boolean,
            stopState = values[9] as ForceStopState,
            lastBatchSummary = values[10] as BatchSummary?,
            lastRunTimestampMs = values[11] as Long?,
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
            canDrawOverlays.value = AccessibilityHelper.canDrawOverlays(context)
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
            val prefs = userPreferencesRepository.prefsFlow.first()
            if (!AccessibilityHelper.canDrawOverlays(context) && !prefs.overlayPromptDismissed) {
                showOverlayDialog.value = true
                return@launch
            }
            executeBatchRun()
        }
    }

    fun grantOverlayPermission() {
        showOverlayDialog.value = false
        runCatching {
            context.startActivity(AccessibilityHelper.createOverlaySettingsIntent(context))
        }
    }

    fun continueWithoutOverlay() {
        showOverlayDialog.value = false
        viewModelScope.launch {
            userPreferencesRepository.setOverlayPromptDismissed(true)
            executeBatchRun()
        }
    }

    fun dismissOverlayDialog() {
        showOverlayDialog.value = false
    }

    private suspend fun executeBatchRun() {
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

        val outcomes = coordinator.runBatch(targets)
        var stopped = 0
        var idle = 0
        var failed = 0
        for (item in outcomes) {
            appRulesRepository.recordHibernationLog(
                packageName = item.packageName,
                appLabel = item.appLabel,
                outcome = item.outcome,
                modeUsed = StopMode.ACCESSIBILITY,
            )
            when (item.outcome) {
                ForceStopOutcome.VERIFIED_STOPPED -> stopped++
                ForceStopOutcome.ALREADY_STOPPED -> idle++
                else -> failed++
            }
        }
        lastBatchSummary.value = BatchSummary(stopped, idle, failed, outcomes)
        isBatchRunning.value = false
        refreshMetrics()
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
