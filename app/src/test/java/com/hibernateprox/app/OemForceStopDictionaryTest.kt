package com.hibernateprox.app

import com.hibernateprox.app.core.engine.OemForceStopDictionary
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OemForceStopDictionaryTest {

    @Test
    fun `settings package check allows known OEM settings and rejects user apps`() {
        assertTrue(OemForceStopDictionary.isSettingsPackage("com.android.settings"))
        assertTrue(OemForceStopDictionary.isSettingsPackage("com.miui.securitycenter"))
        assertTrue(OemForceStopDictionary.isSettingsPackage("com.oplus.appdetail"))
        assertFalse(OemForceStopDictionary.isSettingsPackage("com.whatsapp"))
        assertFalse(OemForceStopDictionary.isSettingsPackage(null))
    }

    @Test
    fun `matches force stop labels across case and OEM locales`() {
        assertTrue(OemForceStopDictionary.matchesForceStopText("Force stop"))
        assertTrue(OemForceStopDictionary.matchesForceStopText("  FORCE STOP "))
        assertTrue(OemForceStopDictionary.matchesForceStopText("Forzar detención"))
        assertTrue(OemForceStopDictionary.matchesForceStopText("强行停止"))
        assertFalse(OemForceStopDictionary.matchesForceStopText("Uninstall"))
    }

    @Test
    fun `matches confirm dialog labels and view ids`() {
        assertTrue(OemForceStopDictionary.matchesConfirmText("OK"))
        assertTrue(OemForceStopDictionary.matchesConfirmText("Force stop"))
        assertTrue(OemForceStopDictionary.matchesConfirmId("android:id/button1"))
        assertFalse(OemForceStopDictionary.matchesConfirmText("Cancel"))
    }
}
