package com.carlosarancibia.playfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picks")
data class PicksEntity(
    @PrimaryKey val gameId: String,
    val title: String,
    val affinityScore: Double,
    val fitReasons: String,
    val genres: String,
    val primaryGenre: String,
    val coverPath: String,
    val addedAt: Long = System.currentTimeMillis(),
)
