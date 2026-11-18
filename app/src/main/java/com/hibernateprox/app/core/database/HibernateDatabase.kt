package com.hibernateprox.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hibernateprox.app.core.database.dao.AppRuleDao
import com.hibernateprox.app.core.database.dao.HibernationLogDao
import com.hibernateprox.app.core.database.dao.ProfileDao
import com.hibernateprox.app.core.database.entity.AppRuleEntity
import com.hibernateprox.app.core.database.entity.HibernationLogEntity
import com.hibernateprox.app.core.database.entity.ProfileEntity

@Database(
    entities = [
        AppRuleEntity::class,
        HibernationLogEntity::class,
        ProfileEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class HibernateDatabase : RoomDatabase() {
    abstract fun appRuleDao(): AppRuleDao
    abstract fun hibernationLogDao(): HibernationLogDao
    abstract fun profileDao(): ProfileDao
}
