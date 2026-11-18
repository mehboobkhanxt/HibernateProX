package com.hibernateprox.app

import com.hibernateprox.app.core.engine.ForegroundAppTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundAppTrackerTest {

    @Test
    fun `tracks distinct foreground transitions and deduplicates repeated same-package events`() {
        val tracker = ForegroundAppTracker()
        tracker.setServiceConnected(true)
        assertTrue(tracker.isServiceConnected.value)

        tracker.onForegroundWindowChanged("com.example.alpha", 100L)
        tracker.onForegroundWindowChanged("com.example.alpha", 150L)
        tracker.onForegroundWindowChanged("com.example.beta", 200L)

        assertEquals("com.example.beta", tracker.currentForegroundPackage.value)
        assertEquals(2, tracker.recentTransitions.value.size)
        assertEquals("com.example.beta", tracker.recentTransitions.value[0].packageName)
        assertEquals("com.example.alpha", tracker.recentTransitions.value[1].packageName)

        tracker.setServiceConnected(false)
        assertFalse(tracker.isServiceConnected.value)
        assertNull(tracker.currentForegroundPackage.value)
    }
}
