package com.hibernateprox.app.core.model

enum class ForceStopOutcome {
    VERIFIED_STOPPED,
    ALREADY_STOPPED,
    BUTTON_NOT_FOUND,
    TIMED_OUT,
    PROTECTED_SKIPPED,
    ACCESSIBILITY_DISABLED,
}

sealed interface ForceStopState {
    data object Idle : ForceStopState
    data class OpeningAppInfo(val packageName: String, val appLabel: String) : ForceStopState
    data class ClickingForceStop(val packageName: String, val appLabel: String) : ForceStopState
    data class ConfirmingDialog(val packageName: String, val appLabel: String) : ForceStopState
    data class Verifying(val packageName: String, val appLabel: String) : ForceStopState
    data class Completed(
        val packageName: String,
        val appLabel: String,
        val outcome: ForceStopOutcome,
    ) : ForceStopState
}

data class ForegroundTransition(
    val packageName: String,
    val timestampMs: Long,
)
