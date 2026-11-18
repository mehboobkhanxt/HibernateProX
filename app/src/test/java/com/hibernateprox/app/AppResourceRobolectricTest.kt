package com.hibernateprox.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppResourceRobolectricTest {

    @Test
    fun `app name matches HibernatePro X`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("HibernatePro X", context.getString(R.string.app_name))
    }
}
