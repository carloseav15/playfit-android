package com.carlosarancibia.playfit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.carlosarancibia.playfit.data.local.entity.GameStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {

    @Query("SELECT * FROM game_states WHERE gameId = :gameId")
    suspend fun getState(gameId: String): GameStateEntity?

    @Query("SELECT * FROM game_states")
    suspend fun getAllStates(): List<GameStateEntity>

    @Query("SELECT * FROM game_states WHERE syncPending = 1")
    suspend fun getPendingSync(): List<GameStateEntity>

    @Query("SELECT COUNT(*) FROM game_states WHERE syncPending = 1")
    suspend fun countPendingSync(): Int

    @Query("SELECT COUNT(*) FROM game_states WHERE syncPending = 1")
    fun observePendingSyncCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: GameStateEntity)

    @Query("UPDATE game_states SET syncPending = 0 WHERE gameId = :gameId")
    suspend fun markSynced(gameId: String)

    @Query("DELETE FROM game_states WHERE gameId = :gameId")
    suspend fun delete(gameId: String)

    @Query("DELETE FROM game_states")
    suspend fun deleteAll()

    @Query("DELETE FROM game_states WHERE syncPending = 0")
    suspend fun deleteSyncedStates()

    @Transaction
    suspend fun replaceSyncedStates(states: List<GameStateEntity>) {
        deleteSyncedStates()
        states.forEach { upsert(it) }
    }
}
