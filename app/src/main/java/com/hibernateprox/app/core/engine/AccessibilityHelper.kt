package com.hibernateprox.app.core.engine

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

object AccessibilityHelper {

    private val validPackageRegex = Regex("^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+$")

    fun isValidPackageName(packageName: String): Boolean =
        validPackageRegex.matches(packageName)

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponent = ComponentName(context, HibernatorAccessibilityService::class.java)
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabledByManager = am?.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        )?.any { service ->
            val info = service.resolveInfo?.serviceInfo ?: return@any false
            info.packageName == expectedComponent.packageName &&
                info.name == expectedComponent.className
        } ?: false

        if (enabledByManager) return true

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val flattened = expectedComponent.flattenToString()
        val shortFlattened = expectedComponent.flattenToShortString()
        return enabledServices.split(':').any { entry ->
            entry.equals(flattened, ignoreCase = true) ||
                entry.equals(shortFlattened, ignoreCase = true)
        }
    }

    fun createAccessibilitySettingsIntent(): Intent =
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    fun createAppInfoIntent(packageName: String): Intent {
        require(isValidPackageName(packageName)) { "Invalid package name: $packageName" }
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
            )
        }
    }

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun createOverlaySettingsIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.fromParts("package", context.packageName, null),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
