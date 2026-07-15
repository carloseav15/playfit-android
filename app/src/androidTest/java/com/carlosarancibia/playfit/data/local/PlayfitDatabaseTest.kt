package com.carlosarancibia.playfit.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.carlosarancibia.playfit.data.local.entity.CacheEntryEntity
import com.carlosarancibia.playfit.data.local.entity.GameStateEntity
import com.carlosarancibia.playfit.data.local.entity.PendingOperationEntity
import com.carlosarancibia.playfit.data.local.entity.PicksEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayfitDatabaseTest {
    private lateinit var database: PlayfitDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            PlayfitDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun picksRoundTripPreservesFieldsAndOrdersByAddedAt() = runBlocking {
        database.picksDao().insertAll(
            listOf(
                pick("older", addedAt = 10L, riskScore = 12.0),
                pick("newer", addedAt = 20L, riskScore = 44.0),
            ),
        )

        val picks = database.picksDao().getAllPicks().first()

        assertEquals(listOf("newer", "older"), picks.map { it.gameId })
        assertEquals(44.0, picks.first().riskScore, 0.0)
        assertEquals("[\"Long campaign\"]", picks.first().cautionReasons)
        assertEquals("High", picks.first().confidence)
    }

    @Test
    fun gameStatePendingOperationAndCacheDaosPersistTheirCriticalFields() = runBlocking {
        database.gameStateDao().upsert(
            GameStateEntity("game-1", "completed", 4.5, false, true, false, false, syncPending = true),
        )
        database.pendingOperationDao().put(
            PendingOperationEntity("op-1", "delete_game_state", "{}", createdAt = 1L),
        )
        database.pendingOperationDao().markAttempt("op-1")
        database.cacheEntryDao().put(CacheEntryEntity("key", "first", updatedAt = 1L))
        database.cacheEntryDao().put(CacheEntryEntity("key", "second", updatedAt = 2L))

        assertTrue(database.gameStateDao().getPendingSync().single().syncPending)
        assertEquals(1, database.pendingOperationDao().getAll().single().attemptCount)
        assertEquals("second", database.cacheEntryDao().get("key")?.payload)

        database.gameStateDao().markSynced("game-1")
        database.pendingOperationDao().delete("op-1")
        database.cacheEntryDao().delete("key")
        assertEquals(0, database.gameStateDao().countPendingSync())
        assertTrue(database.pendingOperationDao().getAll().isEmpty())
        assertNull(database.cacheEntryDao().get("key"))
    }

    private fun pick(gameId: String, addedAt: Long, riskScore: Double) = PicksEntity(
        gameId = gameId,
        title = gameId,
        affinityScore = 80.0,
        riskScore = riskScore,
        confidence = "High",
        fitReasons = "Great combat",
        cautionReasons = "[\"Long campaign\"]",
        platformAvailability = "Available",
        accessStatus = "Playable",
        genres = "RPG",
        primaryGenre = "RPG",
        coverPath = "",
        addedAt = addedAt,
    )
}
