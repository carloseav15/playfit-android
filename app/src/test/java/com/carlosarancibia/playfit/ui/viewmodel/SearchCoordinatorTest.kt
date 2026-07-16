package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.data.DataSource
import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.SearchGamesPage
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.SeedGame
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchCoordinatorTest {
    private val repository = mockk<PlayfitRepository>()
    private val platform = Platform(platformId = "ps5", displayName = "PlayStation 5", family = "playstation")

    @Test
    fun searchUsesSelectedFamilyPlatformsAndDeduplicatesPages() = runTest {
        val first = game("one")
        val second = game("two")
        coEvery { repository.searchGames(any(), any(), any(), any(), any()) } returnsMany listOf(
            RepositoryResult.Success(SearchGamesPage(listOf(first), 2), DataSource.Network),
            RepositoryResult.Success(SearchGamesPage(listOf(first, second), 2), DataSource.Network),
        )
        val coordinator = SearchCoordinator(repository, this, { listOf(platform) }, debounceMs = 0)

        coordinator.updateFamily("playstation")
        coordinator.updateQuery("zelda")
        advanceUntilIdle()
        coordinator.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("one", "two"), coordinator.state.value.results.map { it.gameId })
    }

    private fun game(id: String) = mockk<SeedGame> {
        every { gameId } returns id
    }
}
