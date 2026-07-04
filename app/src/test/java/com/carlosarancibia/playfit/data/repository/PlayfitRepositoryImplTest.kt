package com.carlosarancibia.playfit.data.repository

import com.carlosarancibia.playfit.data.DataSource
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.local.PlayfitDatabase
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.data.local.dao.CacheEntryDao
import com.carlosarancibia.playfit.data.local.dao.GameStateDao
import com.carlosarancibia.playfit.data.local.dao.PendingOperationDao
import com.carlosarancibia.playfit.data.local.dao.PicksDao
import com.carlosarancibia.playfit.data.local.entity.CacheEntryEntity
import com.carlosarancibia.playfit.data.remote.PlayfitApiService
import com.carlosarancibia.playfit.data.remote.RankedSeedGameDto
import com.carlosarancibia.playfit.data.remote.ProfileBuildResponse
import com.carlosarancibia.playfit.data.remote.ProfileDto
import com.carlosarancibia.playfit.data.remote.ProfileSaveRequest
import com.carlosarancibia.playfit.data.remote.SeedGameDto
import com.carlosarancibia.playfit.data.remote.TodayResponse
import com.carlosarancibia.playfit.data.sync.SyncManager
import com.carlosarancibia.playfit.model.ProductOnboardingDraft
import com.carlosarancibia.playfit.model.ProductAccessStatus
import com.carlosarancibia.playfit.model.ProductPlatformSelection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.runs
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayfitRepositoryImplTest {
    private lateinit var api: PlayfitApiService
    private lateinit var database: PlayfitDatabase
    private lateinit var cacheDao: CacheEntryDao
    private lateinit var pendingOperationDao: PendingOperationDao
    private lateinit var gameStateDao: GameStateDao
    private lateinit var picksDao: PicksDao
    private lateinit var authManager: AuthManager
    private lateinit var syncManager: SyncManager
    private lateinit var repository: PlayfitRepositoryImpl

    @Before
    fun setUp() {
        api = mockk()
        database = mockk()
        cacheDao = mockk()
        pendingOperationDao = mockk()
        gameStateDao = mockk()
        picksDao = mockk()
        authManager = mockk(relaxed = true)
        syncManager = mockk(relaxed = true)
        every { database.cacheEntryDao() } returns cacheDao
        every { database.pendingOperationDao() } returns pendingOperationDao
        every { database.gameStateDao() } returns gameStateDao
        every { database.picksDao() } returns picksDao
        repository = PlayfitRepositoryImpl(
            apiService = api,
            database = database,
            preferencesDataStore = mockk<PreferencesDataStore>(relaxed = true),
            authManager = authManager,
            syncManager = syncManager,
        )
    }

    @Test
    fun `today response is cached and reported as network data`() = runTest {
        val response = todayResponse("game-1")
        coEvery { api.getTodayRecommendations() } returns response
        coEvery { cacheDao.put(any()) } just runs

        val result = repository.getTodayRecommendations()

        assertTrue(result is RepositoryResult.Success)
        result as RepositoryResult.Success
        assertEquals(DataSource.Network, result.source)
        assertEquals("game-1", result.data.primary?.game?.gameId)
        coVerify { cacheDao.put(match { it.cacheKey == "recommendations_today" }) }
    }

    @Test
    fun `today uses stale cache when network fails`() = runTest {
        val cached = todayResponse("cached-game")
        coEvery { api.getTodayRecommendations() } throws IOException("offline")
        coEvery { cacheDao.get("recommendations_today") } returns CacheEntryEntity(
            cacheKey = "recommendations_today",
            payload = Json.encodeToString(cached),
        )

        val result = repository.getTodayRecommendations()

        assertTrue(result is RepositoryResult.Success)
        result as RepositoryResult.Success
        assertEquals(DataSource.Cache, result.source)
        assertTrue(result.isStale)
        assertEquals("cached-game", result.data.primary?.game?.gameId)
    }

    @Test
    fun `today returns typed failure when network and cache are unavailable`() = runTest {
        coEvery { api.getTodayRecommendations() } throws IOException("offline")
        coEvery { cacheDao.get("recommendations_today") } returns null

        val result = repository.getTodayRecommendations()

        assertTrue(result is RepositoryResult.Failure)
        assertTrue((result as RepositoryResult.Failure).error is RepositoryError.Network)
    }

    @Test
    fun `offline onboarding is persisted to outbox and scheduled`() = runTest {
        every { authManager.deviceId } returns "device-1"
        coEvery { pendingOperationDao.put(any()) } just runs
        coEvery { gameStateDao.getState(any()) } returns null
        coEvery { gameStateDao.upsert(any()) } just runs
        coEvery { gameStateDao.getAllStates() } returns emptyList()
        coEvery { cacheDao.get("profile_build") } returns null
        coEvery { cacheDao.get(PlayfitRepositoryImpl.CACHE_PROFILE_STATE) } returns null
        coEvery { cacheDao.put(any()) } just runs
        coEvery { api.buildProfile(any()) } throws IOException("offline")

        val result = repository.saveOnboarding(validDraft(), "2026-07-02T00:00:00Z")

        assertTrue(result is RepositoryResult.Success)
        result as RepositoryResult.Success
        assertEquals(DataSource.Local, result.source)
        assertTrue(result.pendingSync)
        coVerify { pendingOperationDao.put(match { it.operationType == PlayfitRepositoryImpl.OPERATION_SAVE_PROFILE }) }
        verify { syncManager.enqueueSync() }
    }

    @Test
    fun `online onboarding builds profile before sending strict save payload`() = runTest {
        val savedRequest = slot<ProfileSaveRequest>()
        every { authManager.deviceId } returns "device-1"
        coEvery { pendingOperationDao.put(any()) } just runs
        coEvery { pendingOperationDao.delete(any()) } just runs
        coEvery { gameStateDao.getState(any()) } returns null
        coEvery { gameStateDao.upsert(any()) } just runs
        coEvery { gameStateDao.getAllStates() } returns emptyList()
        coEvery { cacheDao.put(any()) } just runs
        coEvery { api.buildProfile(any()) } returns ProfileBuildResponse(
            profile = ProfileDto(summary = "Built profile"),
        )
        coEvery { api.saveProfile(capture(savedRequest)) } just runs

        val result = repository.saveOnboarding(validDraft(), "2026-07-02T00:00:00Z")

        assertTrue(result is RepositoryResult.Success)
        result as RepositoryResult.Success
        assertEquals(DataSource.Network, result.source)
        assertEquals("device-1", savedRequest.captured.deviceId)
        assertEquals("Built profile", savedRequest.captured.profile?.summary)
        assertEquals(3, savedRequest.captured.onboarding.likedGameIds.size)
        assertEquals("available", savedRequest.captured.onboarding.platforms.single().status)
        coVerify { pendingOperationDao.delete("onboarding") }
    }

    @Test
    fun `offline signal deletion removes local state and queues remote delete`() = runTest {
        coEvery { gameStateDao.delete("game-1") } just runs
        coEvery { picksDao.delete("game-1") } just runs
        coEvery { pendingOperationDao.put(any()) } just runs
        coEvery { api.deleteGameState("game-1") } throws IOException("offline")

        val result = repository.deleteGameState("game-1")

        assertTrue(result is RepositoryResult.Success)
        result as RepositoryResult.Success
        assertTrue(result.pendingSync)
        coVerify { gameStateDao.delete("game-1") }
        coVerify {
            pendingOperationDao.put(match {
                it.operationType == PlayfitRepositoryImpl.OPERATION_DELETE_GAME_STATE
            })
        }
        verify { syncManager.enqueueSync() }
    }

    private fun todayResponse(gameId: String) = TodayResponse(
        primary = RankedSeedGameDto(
            game = SeedGameDto(gameId = gameId, title = "Game"),
            affinityScore = 90.0,
        ),
        stateVersion = "v1",
    )

    private fun validDraft() = ProductOnboardingDraft(
        platforms = listOf(ProductPlatformSelection("ps5", ProductAccessStatus.Available)),
        likedGameIds = listOf("liked-1", "liked-2", "liked-3"),
        dislikedGameIds = listOf("miss-1"),
    )
}
