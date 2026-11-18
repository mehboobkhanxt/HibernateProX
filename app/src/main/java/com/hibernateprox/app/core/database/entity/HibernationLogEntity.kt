package com.hibernateprox.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hibernation_logs")
data class HibernationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val appLabel: String,
    val outcome: String,
    val modeUsed: String,
    val timestampMs: Long = System.currentTimeMillis(),
)
