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

    @Test
    fun `empty state produces empty model with First look confidence`() {
        val model = ProductTasteDerivation.build(ProductState(), emptyList())

        assertTrue(model.historyEntries.isEmpty())
        assertTrue(model.mapTraits.isEmpty())
        assertEquals(0, model.evidenceCount)
        assertEquals(0, model.positiveCount)
        assertEquals(0, model.negativeCount)
        assertEquals("First look", model.confidenceLabel)
    }

    @Test
    fun `no game states falls back to onboarding-only history`() {
        val state = ProductState(
            user = ProductUserState(
                onboarding = ProductOnboardingDraft(
                    likedGameIds = listOf("liked"),
                    dislikedGameIds = listOf("missed"),
                ),
            ),
        )
        val games = listOf(
            SeedGame("liked", "Liked", primaryGenre = "RPG"),
            SeedGame("missed", "Missed", primaryGenre = "Horror"),
        )

        val model = ProductTasteDerivation.build(state, games)

        assertEquals(2, model.historyEntries.size)
        assertEquals("setup_favorite", model.historyEntries.first { it.gameId == "liked" }.decision)
        assertEquals("setup_miss", model.historyEntries.first { it.gameId == "missed" }.decision)
        assertEquals("onboarding_liked", model.historyEntries.first { it.gameId == "liked" }.source)
    }

    @Test
    fun `rated game takes precedence over the same id in onboarding lists`() {
        val state = ProductState(
            user = ProductUserState(
                onboarding = ProductOnboardingDraft(dislikedGameIds = listOf("game")),
                gameStates = mapOf("game" to ProductGameState("game", "", rating = 5.0)),
            ),
        )
        val games = listOf(SeedGame("game", "Game", primaryGenre = "RPG"))

        val model = ProductTasteDerivation.build(state, games)

        assertEquals(1, model.historyEntries.size)
        assertEquals("liked", model.historyEntries.single().decision)
        assertEquals("rating", model.historyEntries.single().source)
    }

    @Test
    fun `excluded pick with no rating or status is dropped from history`() {
        val state = ProductState(
            user = ProductUserState(
                gameStates = mapOf(
                    "excluded" to ProductGameState("excluded", "", inPlayfitPicks = true, excluded = true),
                ),
            ),
        )

        val model = ProductTasteDerivation.build(state, emptyList())

        assertTrue(model.historyEntries.isEmpty())
    }

    @Test
    fun `abandoned status without a rating counts as a negative dropped entry`() {
        val state = ProductState(
            user = ProductUserState(
                gameStates = mapOf(
                    "abandoned" to ProductGameState("abandoned", "", status = ProductPlayStatus.Abandoned),
                ),
            ),
        )

        val model = ProductTasteDerivation.build(state, emptyList())

        val entry = model.historyEntries.single()
        assertEquals("dropped", entry.decision)
        assertEquals("negative", entry.tone)
        assertEquals(1, model.negativeCount)
    }

    @Test
    fun `profile genre evidence stacks with history evidence for the same trait`() {
        val state = ProductState(
            user = ProductUserState(
                gameStates = mapOf(
                    "rated" to ProductGameState("rated", "", rating = 5.0),
                ),
                profile = ProductProfile(likedGenres = listOf("RPG"), ratedCount = 1),
            ),
        )
        val games = listOf(SeedGame("rated", "Rated", primaryGenre = "RPG"))

        val model = ProductTasteDerivation.build(state, games)

        val rpg = model.mapTraits.first { it.id == "RPG" }
        assertEquals(2, rpg.positiveCount)
    }

    @Test
    fun `confidence label reflects ratedCount thresholds`() {
        fun confidenceFor(ratedCount: Int): String =
            ProductTasteDerivation.build(
                ProductState(user = ProductUserState(profile = ProductProfile(ratedCount = ratedCount))),
                emptyList(),
            ).confidenceLabel

        assertEquals("First look", confidenceFor(4))
        assertEquals("Building signal", confidenceFor(5))
        assertEquals("Building signal", confidenceFor(19))
        assertEquals("Strong signal", confidenceFor(20))
    }
}
