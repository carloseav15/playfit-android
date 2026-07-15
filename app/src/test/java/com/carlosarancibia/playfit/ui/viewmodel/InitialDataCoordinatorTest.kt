package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.data.DataSource
import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.model.fallbackPlatforms
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitialDataCoordinatorTest {
    private lateinit var repository: PlayfitRepository
    private lateinit var preferences: PreferencesDataStore
    private lateinit var coordinator: InitialDataCoordinator

    @Before
    fun setUp() {
        repository = mockk()
        preferences = mockk(relaxed = true)
        coordinator = InitialDataCoordinator(repository, preferences)
    }

    @Test
    fun `starts independent loads together and waits for state before taste`() = runTest {
        val stateGate = CompletableDeferred<Unit>()
        val othersGate = CompletableDeferred<Unit>()
        val started = mutableSetOf<String>()
        coEvery { repository.getState() } coAnswers {
            started += "state"
            stateGate.await()
            success(ProductState())
        }
        coEvery { repository.getPlatforms() } coAnswers {
            started += "platforms"
            othersGate.await()
            success(emptyList())
        }
        coEvery { repository.getTodayRecommendations() } coAnswers {
            started += "today"
            othersGate.await()
            success(ProductPlayNextModel(null, emptyList(), emptyList(), "test"))
        }
        coEvery { repository.getPicks() } coAnswers {
            started += "picks"
            othersGate.await()
            success(emptyList())
        }
        coEvery { repository.getTasteModel() } coAnswers {
            started += "taste"
            success(ProductTasteModel())
        }

        val load = async { coordinator.load() }
        advanceUntilIdle()
        assertEquals(setOf("state", "platforms", "today", "picks"), started)

        stateGate.complete(Unit)
        advanceUntilIdle()
        assertTrue("taste" in started)
        othersGate.complete(Unit)

        val snapshot = load.await()
        assertEquals(fallbackPlatforms, snapshot.platforms)
        assertFalse(snapshot.showingStaleData)
        coVerify { preferences.setOnboardingCompleted(false) }
    }

    @Test
    fun `keeps successful data when one initial load fails`() = runTest {
        coEvery { repository.getPlatforms() } returns success(emptyList())
        coEvery { repository.getState() } returns success(ProductState())
        coEvery { repository.getTodayRecommendations() } returns failure("Today unavailable")
        coEvery { repository.getPicks() } returns success(emptyList(), stale = true)
        coEvery { repository.getTasteModel() } returns success(ProductTasteModel())

        val snapshot = coordinator.load()

        assertNull(snapshot.playNext)
        assertEquals("Today unavailable", snapshot.error)
        assertTrue(snapshot.showingStaleData)
        assertEquals(ProductTasteModel(), snapshot.tasteModel)
    }

    private fun <T> success(data: T, stale: Boolean = false) =
        RepositoryResult.Success(data, DataSource.Network, isStale = stale)

    private fun failure(message: String) = RepositoryResult.Failure(RepositoryError.Network(message))
}
