package com.hibernateprox.app.core.data

import com.hibernateprox.app.core.database.dao.AppRuleDao
import com.hibernateprox.app.core.database.dao.HibernationLogDao
import com.hibernateprox.app.core.database.dao.ProfileDao
import com.hibernateprox.app.core.database.entity.AppRuleEntity
import com.hibernateprox.app.core.database.entity.HibernationLogEntity
import com.hibernateprox.app.core.database.entity.ProfileEntity
import com.hibernateprox.app.core.model.AppRuleMode
import com.hibernateprox.app.core.model.ForceStopOutcome
import com.hibernateprox.app.core.model.StopMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRulesRepository @Inject constructor(
    private val appRuleDao: AppRuleDao,
    private val hibernationLogDao: HibernationLogDao,
    private val profileDao: ProfileDao,
) {
    fun observeRuleMap(): Flow<Map<String, AppRuleMode>> =
        appRuleDao.observeAllRules().map { entities ->
            entities.associate { entity ->
                val mode = runCatching { AppRuleMode.valueOf(entity.ruleMode) }
                    .getOrDefault(AppRuleMode.DEFAULT)
                entity.packageName to mode
            }
        }

    suspend fun setAppRule(packageName: String, mode: AppRuleMode) {
        if (mode == AppRuleMode.DEFAULT) {
            appRuleDao.deleteRule(packageName)
        } else {
            appRuleDao.upsertRule(
                AppRuleEntity(
                    packageName = packageName,
                    ruleMode = mode.name,
                ),
            )
        }
    }

    fun observeLastRunTimestamp(): Flow<Long?> = hibernationLogDao.observeLastRunTimestamp()

    fun observeRecentLogs(limit: Int = 100): Flow<List<HibernationLogEntity>> =
        hibernationLogDao.observeRecentLogs(limit)

    fun observeLatestOutcomeByPackage(): Flow<Map<String, ForceStopOutcome>> =
        hibernationLogDao.observeRecentLogs(250).map { logs ->
            val result = mutableMapOf<String, ForceStopOutcome>()
            for (log in logs) {
                if (!result.containsKey(log.packageName)) {
                    runCatching { ForceStopOutcome.valueOf(log.outcome) }
                        .getOrNull()
                        ?.let { outcome -> result[log.packageName] = outcome }
                }
            }
            result
        }

    suspend fun recordHibernationLog(
        packageName: String,
        appLabel: String,
        outcome: ForceStopOutcome,
        modeUsed: StopMode,
    ) {
        hibernationLogDao.insertLog(
            HibernationLogEntity(
                packageName = packageName,
                appLabel = appLabel,
                outcome = outcome.name,
                modeUsed = modeUsed.name,
            ),
        )
    }

    fun observeProfiles(): Flow<List<ProfileEntity>> = profileDao.observeAllProfiles()

    suspend fun ensureDefaultProfilesSeeded() {
        if (profileDao.count() > 0) return
        profileDao.insertDefaults(
            listOf(
                ProfileEntity(
                    profileKey = "gaming",
                    title = "Gaming",
                    description = "Keep launchers & game services; freeze heavy social & sync apps.",
                    includeSystemApps = false,
                    isBuiltIn = true,
                ),
                ProfileEntity(
                    profileKey = "battery",
                    title = "Battery Saver",
                    description = "Aggressive background cleanup for high-wake packages.",
                    includeSystemApps = false,
                    isBuiltIn = true,
                ),
                ProfileEntity(
                    profileKey = "work",
                    title = "Work Focus",
                    description = "Preserve communication & calendar apps; pause media & games.",
                    includeSystemApps = false,
                    isBuiltIn = true,
                ),
                ProfileEntity(
                    profileKey = "night",
                    title = "Night Vault",
                    description = "Deep standby overnight while protecting alarms & dialer.",
                    includeSystemApps = true,
                    isBuiltIn = true,
                ),
            ),
        )
    }
}
