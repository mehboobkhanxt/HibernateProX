package com.hibernateprox.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hibernateprox.app.core.data.AppRulesRepository
import com.hibernateprox.app.core.database.HibernateDatabase
import com.hibernateprox.app.core.model.AppRuleMode
import com.hibernateprox.app.core.model.ForceStopOutcome
import com.hibernateprox.app.core.model.StopMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppDatabaseRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: HibernateDatabase
    private lateinit var repository: AppRulesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, HibernateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRulesRepository(
            appRuleDao = database.appRuleDao(),
            hibernationLogDao = database.hibernationLogDao(),
            profileDao = database.profileDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `manifest declares zero internet permission`() {
        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        val perms = pkgInfo.requestedPermissions?.toList().orEmpty()
        assertFalse(Manifest.permission.INTERNET in perms)
    }

    @Test
    fun `persists whitelist and blacklist rules and clears on default`() = runTest {
        repository.setAppRule("com.sample.social", AppRuleMode.BLACKLIST)
        repository.setAppRule("com.sample.music", AppRuleMode.WHITELIST)

        val map = repository.observeRuleMap().first()
        assertEquals(AppRuleMode.BLACKLIST, map["com.sample.social"])
        assertEquals(AppRuleMode.WHITELIST, map["com.sample.music"])

        repository.setAppRule("com.sample.social", AppRuleMode.DEFAULT)
        val updated = repository.observeRuleMap().first()
        assertFalse(updated.containsKey("com.sample.social"))
        assertEquals(AppRuleMode.WHITELIST, updated["com.sample.music"])
    }

    @Test
    fun `records hibernation logs and seeds four built-in profiles`() = runTest {
        repository.ensureDefaultProfilesSeeded()
        val profiles = repository.observeProfiles().first()
        assertEquals(4, profiles.size)

        repository.recordHibernationLog(
            packageName = "com.sample.social",
            appLabel = "Social App",
            outcome = ForceStopOutcome.VERIFIED_STOPPED,
            modeUsed = StopMode.ACCESSIBILITY,
        )
        val logs = repository.observeRecentLogs().first()
        assertEquals(1, logs.size)
        assertEquals("com.sample.social", logs.first().packageName)
        assertNotNull(repository.observeLastRunTimestamp().first())
    }
}
