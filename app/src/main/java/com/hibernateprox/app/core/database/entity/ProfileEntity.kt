package com.hibernateprox.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val profileKey: String,
    val title: String,
    val description: String,
    val includeSystemApps: Boolean,
    val isBuiltIn: Boolean,
)
