package com.hibernateprox.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey val packageName: String,
    val ruleMode: String,
    val updatedAtMs: Long = System.currentTimeMillis(),
)
