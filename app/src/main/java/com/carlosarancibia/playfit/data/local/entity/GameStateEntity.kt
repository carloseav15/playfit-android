package com.carlosarancibia.playfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_states")
data class GameStateEntity(
    @PrimaryKey val gameId: String,
    val status: String?,
    val rating: Double?,
    val inPlayfitPicks: Boolean,
    val inBacklog: Boolean,
    val inWishlist: Boolean,
    val excluded: Boolean,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncPending: Boolean = false,
)
