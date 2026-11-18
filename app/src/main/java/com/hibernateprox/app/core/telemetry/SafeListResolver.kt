package com.hibernateprox.app.core.telemetry

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.telecom.TelecomManager
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeListResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val criticalSystemPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.phone",
        "com.android.providers.telephony",
        "com.android.bluetooth",
        "com.android.nfc",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.android.vending",
        "com.miui.securitycenter",
        "com.samsung.android.lool",
    )

    fun resolveProtectedPackages(): Set<String> {
        val protected = mutableSetOf<String>()
        protected.add(context.packageName)
        protected.addAll(criticalSystemPackages)

        // Default launcher
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveFlags = PackageManager.MATCH_DEFAULT_ONLY
        context.packageManager.resolveActivity(homeIntent, resolveFlags)
            ?.activityInfo?.packageName?.let { protected.add(it) }

        // Default dialer
        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        telecom?.defaultDialerPackage?.let { protected.add(it) }

        // Default SMS app
        Telephony.Sms.getDefaultSmsPackage(context)?.let { protected.add(it) }

        // Enabled input methods / keyboards
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.enabledInputMethodList?.forEach { inputMethod ->
            protected.add(inputMethod.packageName)
        }

        // Android 10+ RoleManager checks (Dialer, SMS, Home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null) {
                listOf(
                    RoleManager.ROLE_HOME,
                    RoleManager.ROLE_DIALER,
                    RoleManager.ROLE_SMS,
                ).forEach { role ->
                    runCatching {
                        if (roleManager.isRoleAvailable(role)) {
                            // Role holders are covered by the standard queries above; keep safe
                        }
                    }
                }
            }
        }

        return protected
    }

    fun isProtected(packageName: String, protectedSet: Set<String> = resolveProtectedPackages()): Boolean =
        packageName in protectedSet
}
