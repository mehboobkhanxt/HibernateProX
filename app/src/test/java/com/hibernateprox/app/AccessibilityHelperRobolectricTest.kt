package com.hibernateprox.app

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.hibernateprox.app.core.engine.AccessibilityHelper
import com.hibernateprox.app.core.telemetry.SafeListResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AccessibilityHelperRobolectricTest {

    @Test
    fun `creates valid App Info intent and rejects malformed package names`() {
        assertTrue(AccessibilityHelper.isValidPackageName("com.example.app"))
        assertFalse(AccessibilityHelper.isValidPackageName("com.example.app; rm -rf /"))

        val intent = AccessibilityHelper.createAppInfoIntent("com.example.app")
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:com.example.app", intent.dataString)
    }

    @Test
    fun `safe list always protects self and systemui`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resolver = SafeListResolver(context)
        val protectedSet = resolver.resolveProtectedPackages()

        assertTrue(context.packageName in protectedSet)
        assertTrue("com.android.systemui" in protectedSet)
        assertTrue("com.android.settings" in protectedSet)
    }
}
