package com.carlosarancibia.playfit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.carlosarancibia.playfit.data.local.dao.CacheEntryDao
import com.carlosarancibia.playfit.data.local.dao.GameStateDao
import com.carlosarancibia.playfit.data.local.dao.PendingOperationDao
import com.carlosarancibia.playfit.data.local.dao.PicksDao
import com.carlosarancibia.playfit.data.local.entity.CacheEntryEntity
import com.carlosarancibia.playfit.data.local.entity.GameStateEntity
import com.carlosarancibia.playfit.data.local.entity.PendingOperationEntity
import com.carlosarancibia.playfit.data.local.entity.PicksEntity

@Database(
    entities = [
        GameStateEntity::class,
        PicksEntity::class,
        CacheEntryEntity::class,
        PendingOperationEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class PlayfitDatabase : RoomDatabase() {
    abstract fun gameStateDao(): GameStateDao
    abstract fun picksDao(): PicksDao
    abstract fun cacheEntryDao(): CacheEntryDao
    abstract fun pendingOperationDao(): PendingOperationDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `cache_entries` (`cacheKey` TEXT NOT NULL, `payload` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`cacheKey`))""",
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `pending_operations` (`operationId` TEXT NOT NULL, `operationType` TEXT NOT NULL, `payload` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `attemptCount` INTEGER NOT NULL, PRIMARY KEY(`operationId`))""",
                )
            }
        }
    }
}
