package com.hibernateprox.app.di

import android.content.Context
import androidx.room.Room
import com.hibernateprox.app.core.database.HibernateDatabase
import com.hibernateprox.app.core.database.dao.AppRuleDao
import com.hibernateprox.app.core.database.dao.HibernationLogDao
import com.hibernateprox.app.core.database.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHibernateDatabase(@ApplicationContext context: Context): HibernateDatabase =
        Room.databaseBuilder(
            context,
            HibernateDatabase::class.java,
            "hibernate_pro_x.db",
        ).build()

    @Provides
    fun provideAppRuleDao(db: HibernateDatabase): AppRuleDao = db.appRuleDao()

    @Provides
    fun provideHibernationLogDao(db: HibernateDatabase): HibernationLogDao = db.hibernationLogDao()

    @Provides
    fun provideProfileDao(db: HibernateDatabase): ProfileDao = db.profileDao()
}
