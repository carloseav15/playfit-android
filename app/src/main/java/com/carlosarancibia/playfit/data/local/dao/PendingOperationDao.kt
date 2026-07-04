package com.carlosarancibia.playfit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.carlosarancibia.playfit.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM pending_operations WHERE operationType = :operationType ORDER BY createdAt ASC LIMIT 1")
    suspend fun getFirstByType(operationType: String): PendingOperationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(operation: PendingOperationEntity)

    @Query("DELETE FROM pending_operations WHERE operationId = :operationId")
    suspend fun delete(operationId: String)

    @Query("UPDATE pending_operations SET attemptCount = attemptCount + 1 WHERE operationId = :operationId")
    suspend fun markAttempt(operationId: String)

    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()
}
