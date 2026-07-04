package com.carlosarancibia.playfit.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductTasteDerivationTest {
    @Test
    fun `history combines ratings picks and onboarding without duplicates`() {
        val state = ProductState(
            user = ProductUserState(
                onboarding = ProductOnboardingDraft(
                    likedGameIds = listOf("liked", "rated"),
                    dislikedGameIds = listOf("miss"),
                ),
                gameStates = mapOf(
                    "rated" to ProductGameState("rated", "", rating = 5.0),
                    "pick" to ProductGameState("pick", "", inPlayfitPicks = true),
                ),
            ),
        )
        val games = listOf(
            SeedGame("liked", "Liked", primaryGenre = "RPG", tags = listOf("story_rich")),
            SeedGame("rated", "Rated", primaryGenre = "Action"),
            SeedGame("miss", "Miss", primaryGenre = "Horror"),
            SeedGame("pick", "Pick", primaryGenre = "Puzzle"),
        )

        val model = ProductTasteDerivation.build(state, games)

        assertEquals(4, model.historyEntries.size)
        assertEquals(1, model.historyEntries.count { it.gameId == "rated" })
        assertTrue(model.historyEntries.any { it.decision == "picks" })
        assertTrue(model.historyEntries.any { it.decision == "setup_miss" })
    }

    @Test
    fun `traits combine history and profile evidence`() {
        val state = ProductState(
            user = ProductUserState(
                onboarding = ProductOnboardingDraft(likedGameIds = listOf("liked")),
                profile = ProductProfile(likedGenres = listOf("RPG")),
            ),
        )
        val model = ProductTasteDerivation.build(
            state,
            listOf(SeedGame("liked", "Liked", primaryGenre = "RPG", tags = listOf("story_rich"))),
        )

        val rpg = model.mapTraits.first { it.id == "RPG" }
        assertEquals(2, rpg.positiveCount)
        assertEquals("positive", rpg.direction)
        assertTrue(model.mapTraits.any { it.id == "story_rich" })
    }
}
