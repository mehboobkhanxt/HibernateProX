package com.hibernateprox.app.feature.apps

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
import com.hibernateprox.app.core.model.InstalledAppInfo
import com.hibernateprox.app.core.model.StopMode
import com.hibernateprox.app.core.telemetry.InstalledAppsScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppsFilter {
    ALL,
    ACTIVE,
    USER,
    SYSTEM,
    SAFE_LIST,
    WHITELISTED,
    BLACKLISTED,
}

data class AppsUiState(
    val apps: List<InstalledAppInfo> = emptyList(),
    val filter: AppsFilter = AppsFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isAccessibilityEnabled: Boolean = false,
    val showDisclosureDialog: Boolean = false,
    val stopState: ForceStopState = ForceStopState.Idle,
    val lastOutcomeMessage: Pair<String, ForceStopOutcome>? = null,
)

@HiltViewModel
class AppsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: InstalledAppsScanner,
    private val foregroundAppTracker: ForegroundAppTracker,
    private val coordinator: AccessibilityForceStopCoordinator,
    private val appRulesRepository: AppRulesRepository,
) : ViewModel() {

    private val rawApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val filter = MutableStateFlow(AppsFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)
    private val isAccessibilityEnabled = MutableStateFlow(false)
    private val showDisclosureDialog = MutableStateFlow(false)

    val uiState: StateFlow<AppsUiState> = combine(
        rawApps,
        filter,
        searchQuery,
        isLoading,
        isAccessibilityEnabled,
        showDisclosureDialog,
        coordinator.state,
        foregroundAppTracker.currentForegroundPackage,
        appRulesRepository.observeRuleMap(),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val allApps = values[0] as List<InstalledAppInfo>
        val activeFilter = values[1] as AppsFilter
        val query = values[2] as String
        val loading = values[3] as Boolean
        val a11yEnabled = values[4] as Boolean
        val showDisclosure = values[5] as Boolean
        val stopState = values[6] as ForceStopState
        val fgPkg = values[7] as String?
        @Suppress("UNCHECKED_CAST")
        val ruleMap = values[8] as Map<String, AppRuleMode>

        val filtered = allApps
            .map { app ->
                app.copy(
                    isForegroundNow = app.packageName == fgPkg,
                    ruleMode = if (app.isProtectedSafeList) {
                        AppRuleMode.WHITELIST
                    } else {
                        ruleMap[app.packageName] ?: AppRuleMode.DEFAULT
                    },
                )
            }
            .filter { app ->
                val matchesFilter = when (activeFilter) {
                    AppsFilter.ALL -> true
                    AppsFilter.ACTIVE -> !app.isStopped && !app.isProtectedSafeList
                    AppsFilter.USER -> !app.isSystemApp
                    AppsFilter.SYSTEM -> app.isSystemApp
                    AppsFilter.SAFE_LIST -> app.isProtectedSafeList
                    AppsFilter.WHITELISTED -> !app.isProtectedSafeList && app.ruleMode == AppRuleMode.WHITELIST
                    AppsFilter.BLACKLISTED -> !app.isProtectedSafeList && app.ruleMode == AppRuleMode.BLACKLIST
                }
                val matchesQuery = query.isBlank() ||
                    app.label.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
                matchesFilter && matchesQuery
            }

        val completed = stopState as? ForceStopState.Completed
        AppsUiState(
            apps = filtered,
            filter = activeFilter,
            searchQuery = query,
            isLoading = loading,
            isAccessibilityEnabled = a11yEnabled,
            showDisclosureDialog = showDisclosure,
            stopState = stopState,
            lastOutcomeMessage = completed?.let { it.appLabel to it.outcome },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AppsUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            appRulesRepository.ensureDefaultProfilesSeeded()
            isAccessibilityEnabled.value = AccessibilityHelper.isAccessibilityServiceEnabled(context)
            rawApps.value = scanner.scanInstalledApps(
                foregroundAppTracker.currentForegroundPackage.value,
            )
            isLoading.value = false
        }
    }

    fun setFilter(newFilter: AppsFilter) {
        filter.value = newFilter
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setAppRule(app: InstalledAppInfo, mode: AppRuleMode) {
        if (app.isProtectedSafeList) return
        viewModelScope.launch {
            appRulesRepository.setAppRule(app.packageName, mode)
        }
    }

    fun forceStopApp(app: InstalledAppInfo) {
        if (!AccessibilityHelper.isAccessibilityServiceEnabled(context)) {
            showDisclosureDialog.value = true
            return
        }
        viewModelScope.launch {
            val outcome = coordinator.requestForceStop(app.packageName, app.label)
            appRulesRepository.recordHibernationLog(
                packageName = app.packageName,
                appLabel = app.label,
                outcome = outcome,
                modeUsed = StopMode.ACCESSIBILITY,
            )
            rawApps.value = scanner.scanInstalledApps(
                foregroundAppTracker.currentForegroundPackage.value,
            )
        }
    }

    fun openAppInfoManually(packageName: String) {
        if (!AccessibilityHelper.isValidPackageName(packageName)) return
        runCatching {
            context.startActivity(AccessibilityHelper.createAppInfoIntent(packageName))
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

    fun cancelActiveStop() {
        coordinator.cancelActive()
    }
}
