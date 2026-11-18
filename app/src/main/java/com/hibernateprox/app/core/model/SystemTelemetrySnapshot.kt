package com.hibernateprox.app.core.model

data class SystemTelemetrySnapshot(
    val usedRamGb: Float = 0f,
    val totalRamGb: Float = 0f,
    val availableRamGb: Float = 0f,
    val ramUsageFraction: Float = 0f,
    val freeStorageGb: Float = 0f,
    val totalStorageGb: Float = 0f,
    val storageUsageFraction: Float = 0f,
)
