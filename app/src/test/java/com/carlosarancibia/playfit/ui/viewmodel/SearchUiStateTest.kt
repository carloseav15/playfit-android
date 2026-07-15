package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.model.SeedGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchUiStateTest {

    @Test
    fun `hasMore is false when results already cover the total`() {
        val state = SearchUiState(
            results = listOf(SeedGame(gameId = "g1", title = "Game 1")),
            total = 1,
        )
        assertFalse(state.hasMore)
    }

    @Test
    fun `hasMore is true when total exceeds loaded results`() {
        val state = SearchUiState(
            results = listOf(SeedGame(gameId = "g1", title = "Game 1")),
            total = 5,
        )
        assertTrue(state.hasMore)
    }

    @Test
    fun `hasMore is false for an empty result set with no total`() {
        assertFalse(SearchUiState().hasMore)
    }

    @Test
    fun `mergeSearchResults appends only games not already present`() {
        val existing = listOf(SeedGame(gameId = "g1", title = "Game 1"))
        val incoming = listOf(
            SeedGame(gameId = "g1", title = "Game 1 (stale copy)"),
            SeedGame(gameId = "g2", title = "Game 2"),
        )

        val merged = mergeSearchResults(existing, incoming)

        assertEquals(listOf("g1", "g2"), merged.map { it.gameId })
        assertEquals("Game 1", merged.first().title)
    }

    @Test
    fun `mergeSearchResults with empty existing returns incoming as-is`() {
        val incoming = listOf(SeedGame(gameId = "g1", title = "Game 1"))
        assertEquals(incoming, mergeSearchResults(emptyList(), incoming))
    }
}
