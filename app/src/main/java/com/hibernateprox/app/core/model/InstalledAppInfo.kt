package com.hibernateprox.app.core.model

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val isProtectedSafeList: Boolean,
    val isStopped: Boolean,
    val isForegroundNow: Boolean = false,
    val ruleMode: AppRuleMode = AppRuleMode.DEFAULT,
    val lastStopOutcome: ForceStopOutcome? = null,
)
