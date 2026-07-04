package com.carlosarancibia.playfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey val operationId: String,
    val operationType: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
)
