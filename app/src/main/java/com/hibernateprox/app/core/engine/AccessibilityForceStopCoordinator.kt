package com.hibernateprox.app.core.engine

import android.content.Context
import android.content.Intent
import com.hibernateprox.app.core.model.ForceStopOutcome
import com.hibernateprox.app.core.model.ForceStopState
import com.hibernateprox.app.core.model.InstalledAppInfo
import com.hibernateprox.app.core.telemetry.InstalledAppsScanner
import com.hibernateprox.app.core.telemetry.SafeListResolver
import com.hibernateprox.app.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class BatchAppOutcome(
    val packageName: String,
    val appLabel: String,
    val outcome: ForceStopOutcome,
)

@Singleton
class AccessibilityForceStopCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: InstalledAppsScanner,
    private val safeListResolver: SafeListResolver,
    private val overlayController: HibernateOverlayController,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()

    private val _state = MutableStateFlow<ForceStopState>(ForceStopState.Idle)
    val state: StateFlow<ForceStopState> = _state.asStateFlow()

    @Volatile
    var currentTargetPackage: String? = null
        private set

    @Volatile
    var isWaitingForConfirm: Boolean = false
        private set

    @Volatile
    private var cancelRequested: Boolean = false

    @Volatile
    private var activeCompletion: CompletableDeferred<ForceStopOutcome>? = null

    suspend fun requestForceStop(packageName: String, appLabel: String): ForceStopOutcome =
        mutex.withLock {
            cancelRequested = false
            try {
                overlayController.showOrUpdate(
                    currentIndex = 1,
                    totalCount = 1,
                    appLabel = appLabel,
                    onCancel = ::cancelActive,
                )
                executeSingleLocked(packageName, appLabel)
            } finally {
                overlayController.hide()
                returnToApp()
            }
        }

    suspend fun runBatch(targets: List<InstalledAppInfo>): List<BatchAppOutcome> =
        mutex.withLock {
            cancelRequested = false
            val results = mutableListOf<BatchAppOutcome>()
            val total = targets.size
            try {
                for ((index, target) in targets.withIndex()) {
                    if (cancelRequested) break
                    overlayController.showOrUpdate(
                        currentIndex = index + 1,
                        totalCount = total,
                        appLabel = target.label,
                        onCancel = ::cancelActive,
                    )
                    val outcome = executeSingleLocked(target.packageName, target.label)
                    results += BatchAppOutcome(
                        packageName = target.packageName,
                        appLabel = target.label,
                        outcome = outcome,
                    )
                }
            } finally {
                overlayController.hide()
                returnToApp()
            }
            results
        }

    private suspend fun executeSingleLocked(
        packageName: String,
        appLabel: String,
    ): ForceStopOutcome {
        if (!AccessibilityHelper.isValidPackageName(packageName) ||
            safeListResolver.isProtected(packageName)
        ) {
            _state.value = ForceStopState.Completed(
                packageName,
                appLabel,
                ForceStopOutcome.PROTECTED_SKIPPED,
            )
            return ForceStopOutcome.PROTECTED_SKIPPED
        }

        if (!AccessibilityHelper.isAccessibilityServiceEnabled(context)) {
            _state.value = ForceStopState.Completed(
                packageName,
                appLabel,
                ForceStopOutcome.ACCESSIBILITY_DISABLED,
            )
            return ForceStopOutcome.ACCESSIBILITY_DISABLED
        }

        if (scanner.isPackageStopped(packageName)) {
            _state.value = ForceStopState.Completed(
                packageName,
                appLabel,
                ForceStopOutcome.ALREADY_STOPPED,
            )
            return ForceStopOutcome.ALREADY_STOPPED
        }

        val deferred = CompletableDeferred<ForceStopOutcome>()
        activeCompletion = deferred
        currentTargetPackage = packageName
        isWaitingForConfirm = false
        _state.value = ForceStopState.OpeningAppInfo(packageName, appLabel)

        withContext(ioDispatcher) {
            runCatching {
                context.startActivity(AccessibilityHelper.createAppInfoIntent(packageName))
            }.onFailure {
                deferred.complete(ForceStopOutcome.BUTTON_NOT_FOUND)
            }
        }

        val rawOutcome = withTimeoutOrNull(STEP_TIMEOUT_MS) {
            deferred.await()
        } ?: ForceStopOutcome.TIMED_OUT

        currentTargetPackage = null
        isWaitingForConfirm = false
        activeCompletion = null

        _state.value = ForceStopState.Verifying(packageName, appLabel)
        delay(VERIFY_SETTLE_DELAY_MS)
        val verifiedStopped = scanner.isPackageStopped(packageName)

        val finalOutcome = when {
            rawOutcome == ForceStopOutcome.ALREADY_STOPPED && verifiedStopped ->
                ForceStopOutcome.ALREADY_STOPPED
            verifiedStopped -> ForceStopOutcome.VERIFIED_STOPPED
            rawOutcome == ForceStopOutcome.TIMED_OUT -> ForceStopOutcome.TIMED_OUT
            else -> ForceStopOutcome.BUTTON_NOT_FOUND
        }

        _state.value = ForceStopState.Completed(packageName, appLabel, finalOutcome)
        return finalOutcome
    }

    fun cancelActive() {
        cancelRequested = true
        currentTargetPackage = null
        isWaitingForConfirm = false
        activeCompletion?.complete(ForceStopOutcome.TIMED_OUT)
        activeCompletion = null
        overlayController.hide()
        _state.value = ForceStopState.Idle
    }

    private fun returnToApp() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return
        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
        )
        runCatching { context.startActivity(launchIntent) }
    }

    internal fun onSettingsWindowEntered() {
        val pkg = currentTargetPackage ?: return
        val current = _state.value
        if (current is ForceStopState.OpeningAppInfo && current.packageName == pkg) {
            _state.value = ForceStopState.ClickingForceStop(pkg, current.appLabel)
        }
    }

    internal fun onForceStopClicked() {
        val pkg = currentTargetPackage ?: return
        val label = when (val s = _state.value) {
            is ForceStopState.OpeningAppInfo -> s.appLabel
            is ForceStopState.ClickingForceStop -> s.appLabel
            else -> pkg
        }
        isWaitingForConfirm = true
        _state.value = ForceStopState.ConfirmingDialog(pkg, label)
    }

    internal fun onForceStopDisabledDetected() {
        activeCompletion?.complete(ForceStopOutcome.ALREADY_STOPPED)
    }

    internal fun onConfirmClicked() {
        activeCompletion?.complete(ForceStopOutcome.VERIFIED_STOPPED)
    }

    companion object {
        private const val STEP_TIMEOUT_MS = 4_500L
        private const val VERIFY_SETTLE_DELAY_MS = 350L
    }
}
