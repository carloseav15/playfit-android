package com.carlosarancibia.playfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries")
data class CacheEntryEntity(
    @PrimaryKey val cacheKey: String,
    val payload: String,
    val updatedAt: Long = System.currentTimeMillis(),
)
