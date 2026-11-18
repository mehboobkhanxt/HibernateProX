package com.hibernateprox.app.core.telemetry

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.hibernateprox.app.core.model.InstalledAppInfo
import com.hibernateprox.app.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safeListResolver: SafeListResolver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun scanInstalledApps(foregroundPackage: String?): List<InstalledAppInfo> =
        withContext(ioDispatcher) {
            val pm = context.packageManager
            val protectedSet = safeListResolver.resolveProtectedPackages()
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            packages
                .asSequence()
                .map { appInfo ->
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isStopped = (appInfo.flags and ApplicationInfo.FLAG_STOPPED) != 0
                    val label = runCatching { pm.getApplicationLabel(appInfo).toString() }
                        .getOrDefault(appInfo.packageName)
                    InstalledAppInfo(
                        packageName = appInfo.packageName,
                        label = label,
                        isSystemApp = isSystem,
                        isProtectedSafeList = appInfo.packageName in protectedSet,
                        isStopped = isStopped,
                        isForegroundNow = appInfo.packageName == foregroundPackage,
                    )
                }
                .sortedWith(
                    compareBy<InstalledAppInfo> { it.isStopped }
                        .thenBy { it.isProtectedSafeList }
                        .thenBy { it.isSystemApp }
                        .thenBy { it.label.lowercase() },
                )
                .toList()
        }

    suspend fun isPackageStopped(packageName: String): Boolean = withContext(ioDispatcher) {
        runCatching {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            (info.flags and ApplicationInfo.FLAG_STOPPED) != 0
        }.getOrDefault(false)
    }

    fun getAppLabel(packageName: String): String = runCatching {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    }.getOrDefault(packageName)
}
