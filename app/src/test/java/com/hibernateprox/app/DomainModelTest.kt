package com.hibernateprox.app

import com.hibernateprox.app.core.model.ProfilePreset
import com.hibernateprox.app.core.model.StopMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelTest {

    @Test
    fun `stop mode priority prefers shizuku over root and accessibility`() {
        assertTrue(StopMode.SHIZUKU.priorityRank < StopMode.ROOT.priorityRank)
        assertTrue(StopMode.ROOT.priorityRank < StopMode.ACCESSIBILITY.priorityRank)
        assertTrue(StopMode.ACCESSIBILITY.priorityRank < StopMode.UNAVAILABLE.priorityRank)
    }

    @Test
    fun `default profile presets include all four core modes`() {
        assertEquals(4, ProfilePreset.entries.size)
    }
}
