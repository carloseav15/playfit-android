package com.carlosarancibia.playfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picks")
data class PicksEntity(
    @PrimaryKey val gameId: String,
    val title: String,
    val affinityScore: Double,
    val riskScore: Double,
    val confidence: String,
    val fitReasons: String,
    val cautionReasons: String,
    val platformAvailability: String,
    val accessStatus: String,
    val genres: String,
    val primaryGenre: String,
    val coverPath: String,
    val addedAt: Long = System.currentTimeMillis(),
)
