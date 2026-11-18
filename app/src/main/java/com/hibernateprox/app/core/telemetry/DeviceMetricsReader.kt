package com.hibernateprox.app.core.telemetry

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.hibernateprox.app.core.model.SystemTelemetrySnapshot
import com.hibernateprox.app.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMetricsReader @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun readSnapshot(): SystemTelemetrySnapshot = withContext(ioDispatcher) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)

        val totalRamBytes = memInfo.totalMem.coerceAtLeast(1L)
        val availRamBytes = memInfo.availMem.coerceIn(0L, totalRamBytes)
        val usedRamBytes = (totalRamBytes - availRamBytes).coerceAtLeast(0L)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorageBytes = statFs.totalBytes.coerceAtLeast(1L)
        val freeStorageBytes = statFs.availableBytes.coerceIn(0L, totalStorageBytes)
        val usedStorageBytes = (totalStorageBytes - freeStorageBytes).coerceAtLeast(0L)

        val bytesPerGb = 1024f * 1024f * 1024f
        SystemTelemetrySnapshot(
            usedRamGb = usedRamBytes / bytesPerGb,
            totalRamGb = totalRamBytes / bytesPerGb,
            availableRamGb = availRamBytes / bytesPerGb,
            ramUsageFraction = (usedRamBytes.toFloat() / totalRamBytes.toFloat()).coerceIn(0f, 1f),
            freeStorageGb = freeStorageBytes / bytesPerGb,
            totalStorageGb = totalStorageBytes / bytesPerGb,
            storageUsageFraction = (usedStorageBytes.toFloat() / totalStorageBytes.toFloat()).coerceIn(0f, 1f),
        )
    }
}
