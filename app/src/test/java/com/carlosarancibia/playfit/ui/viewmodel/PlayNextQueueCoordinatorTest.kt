package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.model.ProductConfidence
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.GameAccessStatus
import com.carlosarancibia.playfit.model.PlatformAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayNextQueueCoordinatorTest {
    private val coordinator = PlayNextQueueCoordinator()

    @Test
    fun `saved pick is added once and can be removed`() {
        val saved = coordinator.withSavedPick(model(primary = "one"), "one", picked = true)
        val savedAgain = coordinator.withSavedPick(saved, "one", picked = true)

        assertEquals(listOf("one"), savedAgain?.savedPickIds)
        assertTrue(coordinator.withSavedPick(savedAgain, "one", picked = false)?.savedPickIds.orEmpty().isEmpty())
        assertNull(coordinator.withSavedPick(null, "one", picked = true))
    }

    @Test
    fun `removing primary promotes first alternative without duplicates`() {
        val result = coordinator.withoutRecommendation(model(primary = "one", alternatives = listOf("two", "three")), "one")

        assertEquals("two", result?.primary?.game?.gameId)
        assertEquals(listOf("three"), result?.alternatives?.map { it.game.gameId })
    }

    @Test
    fun `refreshes only when remaining visible recommendations reach threshold`() {
        assertTrue(coordinator.shouldRefreshAfterAction(null))
        assertTrue(coordinator.shouldRefreshAfterAction(model(primary = "one")))
        assertFalse(coordinator.shouldRefreshAfterAction(model(primary = "one", alternatives = listOf("two"))))
    }

    @Test
    fun `fresh merge rejects duplicates and filters excluded results`() {
        val current = model(primary = "one", alternatives = listOf("two"))
        assertEquals(current, coordinator.mergeFreshIfNew(current, model(primary = "one", alternatives = listOf("two")), emptySet()))

        val merged = coordinator.mergeFreshIfNew(current, model(primary = "three", alternatives = listOf("two", "four")), setOf("three"))
        assertNull(merged?.primary)
        assertEquals(listOf("two", "four"), merged?.alternatives?.map { it.game.gameId })
    }

    private fun model(primary: String?, alternatives: List<String> = emptyList()) = ProductPlayNextModel(
        primary = primary?.let(::recommendation),
        alternatives = alternatives.map(::recommendation),
        savedPickIds = emptyList(),
        stateVersion = "test",
    )

    private fun recommendation(id: String) = RankedSeedGame(
        game = SeedGame(gameId = id, title = id),
        affinityScore = 0.5,
        riskScore = 0.0,
        confidence = ProductConfidence.Medium,
        fitReasons = emptyList(),
        cautionReasons = emptyList(),
        platformAvailability = PlatformAvailability.Unknown,
        accessStatus = GameAccessStatus.Playable,
    )
}
