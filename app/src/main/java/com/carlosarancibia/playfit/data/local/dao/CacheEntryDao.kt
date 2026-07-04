package com.carlosarancibia.playfit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.carlosarancibia.playfit.data.local.entity.CacheEntryEntity

@Dao
interface CacheEntryDao {
    @Query("SELECT * FROM cache_entries WHERE cacheKey = :key")
    suspend fun get(key: String): CacheEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: CacheEntryEntity)

    @Query("DELETE FROM cache_entries WHERE cacheKey = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM cache_entries")
    suspend fun deleteAll()
}
