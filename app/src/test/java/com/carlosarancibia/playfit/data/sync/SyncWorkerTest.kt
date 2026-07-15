package com.carlosarancibia.playfit.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.carlosarancibia.playfit.data.local.PlayfitDatabase
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.data.local.dao.GameStateDao
import com.carlosarancibia.playfit.data.local.dao.PendingOperationDao
import com.carlosarancibia.playfit.data.local.entity.GameStateEntity
import com.carlosarancibia.playfit.data.local.entity.PendingOperationEntity
import com.carlosarancibia.playfit.data.remote.PlayfitApiService
import com.carlosarancibia.playfit.data.remote.GameStateDto
import com.carlosarancibia.playfit.data.remote.PersistedOnboardingDto
import com.carlosarancibia.playfit.data.remote.ProfilePersistedState
import com.carlosarancibia.playfit.data.remote.ProfileStateResponse
import com.carlosarancibia.playfit.data.repository.PlayfitRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncWorkerTest {
    private lateinit var database: PlayfitDatabase
    private lateinit var gameStates: GameStateDao
    private lateinit var operations: PendingOperationDao
    private lateinit var api: PlayfitApiService
    private lateinit var worker: SyncWorker

    @Before
    fun setUp() {
        database = mockk()
        gameStates = mockk()
        operations = mockk()
        api = mockk()
        every { database.gameStateDao() } returns gameStates
        every { database.pendingOperationDao() } returns operations
        worker = SyncWorker(
            context = mockk<Context>(relaxed = true),
            params = mockk<WorkerParameters>(relaxed = true),
            database = database,
            apiService = api,
            preferencesDataStore = mockk<PreferencesDataStore>(relaxed = true),
        )
    }

    @Test
    fun `succeeds without pending work`() = runTest {
        coEvery { operations.getFirstByType(PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE) } returns null
        coEvery { gameStates.getPendingSync() } returns emptyList()
        coEvery { operations.getAll() } returns emptyList()

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
    }

    @Test
    fun `batches pending game states and preserves remote states`() = runTest {
        val completed = GameStateEntity("game-1", "completed", 5.0, false, false, false, false, syncPending = true)
        val excluded = GameStateEntity("game-3", null, null, false, false, false, true, syncPending = true)
        val remoteState = GameStateDto(gameId = "game-2", title = "Remote game")
        val remote = ProfileStateResponse(
            ProfilePersistedState(
                gameStates = mapOf("game-2" to remoteState),
                onboarding = PersistedOnboardingDto("complete", emptyList(), emptyList(), emptyList()),
            ),
        )
        coEvery { operations.getFirstByType(PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE) } returns null
        coEvery { gameStates.getPendingSync() } returns listOf(completed, excluded)
        coEvery { operations.getAll() } returns emptyList()
        coEvery { api.getProfile() } returns remote
        coEvery { api.saveProfile(any()) } just runs
        coEvery { database.cacheEntryDao().put(any()) } just runs
        coEvery { gameStates.markSynced(any()) } just runs
        coEvery { gameStates.countPendingSync() } returns 0
        coEvery { operations.getAll() } returns emptyList()

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        coVerify {
            api.saveProfile(match {
                it.gameStates.keys == setOf("game-1", "game-2", "game-3") &&
                    it.gameStates.getValue("game-1").status == "completed" &&
                    it.gameStates.getValue("game-1").rating == 5.0 &&
                    it.gameStates.getValue("game-3").excluded == true
            })
        }
        coVerify(exactly = 0) { api.upsertGameState(any(), any()) }
        coVerify { gameStates.markSynced("game-1") }
        coVerify { gameStates.markSynced("game-3") }
    }

    @Test
    fun `returns retry when syncing a game state fails from network`() = runTest {
        val state = GameStateEntity("game-1", null, null, false, false, false, true, syncPending = true)
        coEvery { operations.getFirstByType(PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE) } returns null
        coEvery { gameStates.getPendingSync() } returns listOf(state)
        coEvery { operations.getAll() } returns emptyList()
        coEvery { api.getProfile() } throws IOException("offline")
        coEvery { gameStates.countPendingSync() } returns 1

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        coVerify(exactly = 0) { gameStates.markSynced("game-1") }
    }

    @Test
    fun `fails permanently without calling an operation that exhausted retries`() = runTest {
        val operation = PendingOperationEntity(
            operationId = "bad-operation",
            operationType = PlayfitRepositoryImpl.OPERATION_DELETE_GAME_STATE,
            payload = "{\"gameId\":\"game-1\"}",
            attemptCount = SyncWorker.MAX_OPERATION_ATTEMPTS,
        )
        coEvery { operations.getFirstByType(PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE) } returns null
        coEvery { gameStates.getPendingSync() } returns emptyList()
        coEvery { operations.getAll() } returns listOf(operation)
        coEvery { gameStates.countPendingSync() } returns 0

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        coVerify(exactly = 0) { operations.markAttempt("bad-operation") }
        coVerify(exactly = 0) { api.deleteGameState(any()) }
    }
}
