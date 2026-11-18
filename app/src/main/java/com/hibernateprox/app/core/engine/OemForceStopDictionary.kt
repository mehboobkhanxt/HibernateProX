package com.hibernateprox.app.core.engine

import java.util.Locale

object OemForceStopDictionary {

    val settingsPackages: Set<String> = setOf(
        "com.android.settings",
        "com.miui.securitycenter",
        "com.oplus.appdetail",
        "com.coloros.appdetail",
        "com.vivo.appdetail",
        "com.iqoo.secure",
        "com.samsung.android.lool",
        "com.huawei.systemmanager",
        "com.oneplus.security",
        "com.transsion.phonemaster",
        "com.transsion.appdetail",
    )

    val forceStopViewIds: Set<String> = setOf(
        "com.android.settings:id/right_button",
        "com.android.settings:id/force_stop_button",
        "com.android.settings:id/button2",
        "com.miui.securitycenter:id/action_force_stop",
        "com.oplus.appdetail:id/force_stop",
        "com.coloros.appdetail:id/force_stop",
    )

    val confirmDialogViewIds: Set<String> = setOf(
        "android:id/button1",
        "com.android.settings:id/button1",
        "androidx.appcompat:id/button1",
        "miui:id/button1",
    )

    private val forceStopLabels: Set<String> = setOf(
        "force stop",
        "forcestop",
        "force-stop",
        "end",
        "stop",
        "forzar detención",
        "forzar detencion",
        "detener",
        "forcer l'arrêt",
        "forcer l’arrêt",
        "beenden erzwingen",
        "interrompi",
        "forçar parada",
        "paksa berhenti",
        "остановить",
        "zorla durdur",
        "فرض الإيقاف",
        "إيقاف إجباري",
        "强行停止",
        "結束",
        "강제 중지",
        "強制停止",
        "فورس سٹاپ",
        "جبری بند کریں",
    )

    private val confirmLabels: Set<String> = setOf(
        "ok",
        "force stop",
        "end",
        "confirm",
        "yes",
        "aceptar",
        "forzar detención",
        "confirmer",
        "erzwingen",
        "beenden",
        "concordo",
        "sim",
        "oke",
        "paksa berhenti",
        "да",
        "tamam",
        "موافق",
        "حسناً",
        "确定",
        "確定",
        "확인",
        "はい",
        "ٹھیک ہے",
    )

    fun isSettingsPackage(packageName: String?): Boolean =
        packageName != null && packageName in settingsPackages

    fun matchesForceStopId(viewId: String?): Boolean =
        viewId != null && viewId in forceStopViewIds

    fun matchesConfirmId(viewId: String?): Boolean =
        viewId != null && viewId in confirmDialogViewIds

    fun matchesForceStopText(rawText: CharSequence?): Boolean {
        val normalized = normalize(rawText) ?: return false
        return normalized in forceStopLabels || normalized.contains("force stop")
    }

    fun matchesConfirmText(rawText: CharSequence?): Boolean {
        val normalized = normalize(rawText) ?: return false
        return normalized in confirmLabels
    }

    private fun normalize(rawText: CharSequence?): String? {
        val trimmed = rawText?.toString()?.trim()?.lowercase(Locale.ROOT)
        return if (trimmed.isNullOrEmpty()) null else trimmed
    }
}
