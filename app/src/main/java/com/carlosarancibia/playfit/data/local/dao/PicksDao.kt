package com.carlosarancibia.playfit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.carlosarancibia.playfit.data.local.entity.PicksEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PicksDao {

    @Query("SELECT * FROM picks ORDER BY addedAt DESC")
    fun getAllPicks(): Flow<List<PicksEntity>>

    @Query("SELECT * FROM picks WHERE gameId = :gameId")
    suspend fun getPick(gameId: String): PicksEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pick: PicksEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(picks: List<PicksEntity>)

    @Query("DELETE FROM picks WHERE gameId = :gameId")
    suspend fun delete(gameId: String)

    @Query("DELETE FROM picks")
    suspend fun deleteAll()
}
