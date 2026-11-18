package com.hibernateprox.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hibernateprox.app.core.database.entity.HibernationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HibernationLogDao {
    @Query("SELECT * FROM hibernation_logs ORDER BY timestampMs DESC, id DESC LIMIT :limit")
    fun observeRecentLogs(limit: Int = 100): Flow<List<HibernationLogEntity>>

    @Query("SELECT MAX(timestampMs) FROM hibernation_logs")
    fun observeLastRunTimestamp(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HibernationLogEntity)

    @Query("DELETE FROM hibernation_logs")
    suspend fun clearAll()
}
