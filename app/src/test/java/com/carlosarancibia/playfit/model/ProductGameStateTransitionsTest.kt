package com.carlosarancibia.playfit.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductGameStateTransitionsTest {

    private val timestamp = "2026-07-02T12:00:00Z"

    @Test
    fun `played loved maps to completed rating five and removes pick`() {
        val next = ProductGameStateTransitions.applyFeedback(
            existing = state(inPlayfitPicks = true, inBacklog = true),
            gameId = "hades",
            feedback = ProductDecisionFeedback.PlayedLoved,
            timestamp = timestamp,
        )

        assertEquals(ProductPlayStatus.Completed, next.status)
        assertEquals(5.0, next.rating)
        assertFalse(next.inPlayfitPicks)
        assertFalse(next.inBacklog)
        assertFalse(next.excluded)
    }

    @Test
    fun `played dropped maps to abandoned rating two and excluded`() {
        val next = ProductGameStateTransitions.applyFeedback(
            existing = state(inPlayfitPicks = true),
            gameId = "hades",
            feedback = ProductDecisionFeedback.PlayedDropped,
            timestamp = timestamp,
        )

        assertEquals(ProductPlayStatus.Abandoned, next.status)
        assertEquals(2.0, next.rating)
        assertTrue(next.excluded)
        assertFalse(next.inPlayfitPicks)
    }

    @Test
    fun `not for me preserves status but sets negative rating and excluded`() {
        val next = ProductGameStateTransitions.applyFeedback(
            existing = state(status = ProductPlayStatus.OnHold, inPlayfitPicks = true),
            gameId = "hades",
            feedback = ProductDecisionFeedback.NotForMe,
            timestamp = timestamp,
        )

        assertEquals(ProductPlayStatus.OnHold, next.status)
        assertEquals(2.0, next.rating)
        assertTrue(next.excluded)
        assertFalse(next.inPlayfitPicks)
    }

    @Test
    fun `play moves game to playing without inventing rating`() {
        val next = ProductGameStateTransitions.applyFeedback(
            existing = state(inPlayfitPicks = true, inBacklog = true, excluded = true),
            gameId = "hades",
            feedback = ProductDecisionFeedback.Play,
            timestamp = timestamp,
        )

        assertEquals(ProductPlayStatus.Playing, next.status)
        assertNull(next.rating)
        assertFalse(next.inPlayfitPicks)
        assertFalse(next.inBacklog)
        assertFalse(next.excluded)
    }

    @Test
    fun `later shelves game and preserves its pick like iOS`() {
        val next = ProductGameStateTransitions.applyFeedback(
            existing = state(inPlayfitPicks = true),
            gameId = "hades",
            feedback = ProductDecisionFeedback.Later,
            timestamp = timestamp,
        )

        assertEquals(ProductPlayStatus.Shelved, next.status)
        assertTrue(next.inBacklog)
        assertTrue(next.inPlayfitPicks)
        assertFalse(next.excluded)
    }

    @Test
    fun `non played ratings preserve unrelated fields`() {
        val next = ProductGameStateTransitions.applyFeedback(
            existing = state(
                status = ProductPlayStatus.Playing,
                inPlayfitPicks = true,
                inWishlist = true,
            ),
            gameId = "hades",
            feedback = ProductDecisionFeedback.Liked,
            timestamp = timestamp,
        )

        assertEquals(4.0, next.rating)
        assertEquals(ProductPlayStatus.Playing, next.status)
        assertTrue(next.inPlayfitPicks)
        assertTrue(next.inWishlist)
    }

    @Test
    fun `setting pick does not erase rating status or flags`() {
        val next = ProductGameStateTransitions.setPick(
            existing = state(
                status = ProductPlayStatus.Completed,
                rating = 5.0,
                inBacklog = true,
                inWishlist = true,
                excluded = true,
            ),
            gameId = "hades",
            picked = true,
            timestamp = timestamp,
        )

        assertTrue(next.inPlayfitPicks)
        assertEquals(5.0, next.rating)
        assertEquals(ProductPlayStatus.Completed, next.status)
        assertTrue(next.inBacklog)
        assertTrue(next.inWishlist)
        assertTrue(next.excluded)
    }

    @Test
    fun `new state receives stable timestamps`() {
        val next = ProductGameStateTransitions.setPick(
            existing = null,
            gameId = "hades",
            picked = true,
            timestamp = timestamp,
        )

        assertEquals(timestamp, next.createdAt)
        assertEquals(timestamp, next.updatedAt)
    }

    @Test
    fun `play status serializes to backend values`() {
        assertEquals("playing", ProductPlayStatus.Playing.apiValue)
        assertEquals("on_hold", ProductPlayStatus.OnHold.apiValue)
        assertEquals("want_to_play", ProductPlayStatus.WantToPlay.apiValue)
    }

    @Test
    fun `play status parses backend and legacy values`() {
        assertEquals(ProductPlayStatus.Completed, ProductPlayStatus.fromApiValue("completed"))
        assertEquals(ProductPlayStatus.OnHold, ProductPlayStatus.fromApiValue("on_hold"))
        assertEquals(ProductPlayStatus.OnHold, ProductPlayStatus.fromApiValue("OnHold"))
        assertNull(ProductPlayStatus.fromApiValue("unknown"))
    }

    private fun state(
        status: ProductPlayStatus? = null,
        rating: Double? = null,
        inBacklog: Boolean = false,
        inWishlist: Boolean = false,
        inPlayfitPicks: Boolean = false,
        excluded: Boolean = false,
    ) = ProductGameState(
        gameId = "hades",
        title = "Hades",
        status = status,
        rating = rating,
        inBacklog = inBacklog,
        inWishlist = inWishlist,
        inPlayfitPicks = inPlayfitPicks,
        excluded = excluded,
        source = "manual",
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )
}
