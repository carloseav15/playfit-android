package com.carlosarancibia.playfit.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinateCalculationTest {

    @Test
    fun `demanding tags push X positive`() {
        val (x, _) = calculateGameCoordinates(
            tags = listOf("souls_like", "demanding"),
            primaryGenre = "action",
            gameId = "test1",
        )
        assertTrue("Demanding tags should push X positive, got $x", x > 0)
    }

    @Test
    fun `chill tags push X negative`() {
        val (x, _) = calculateGameCoordinates(
            tags = listOf("chill", "cozy"),
            primaryGenre = "adventure",
            gameId = "test2",
        )
        assertTrue("Chill tags should push X negative, got $x", x < 0)
    }

    @Test
    fun `systems tags push Y positive`() {
        val (_, y) = calculateGameCoordinates(
            tags = listOf("open_world", "sandbox"),
            primaryGenre = "action",
            gameId = "test3",
        )
        assertTrue("Systems tags should push Y positive, got $y", y > 0)
    }

    @Test
    fun `story tags push Y negative`() {
        val (_, y) = calculateGameCoordinates(
            tags = listOf("story_rich", "lore_heavy"),
            primaryGenre = "rpg",
            gameId = "test4",
        )
        assertTrue("Story tags should push Y negative, got $y", y < 0)
    }

    @Test
    fun `RPG genre falls back to story-cozy quadrant`() {
        val (x, y) = calculateGameCoordinates(
            tags = emptyList(),
            primaryGenre = "role_playing",
            gameId = "test5",
        )
        assertTrue("RPG X should be >= -90, got $x", x >= -90.0)
        assertTrue("RPG Y should be <= 90, got $y", y <= 90.0)
    }

    @Test
    fun `Action genre falls back to demanding-systems quadrant`() {
        val (x, y) = calculateGameCoordinates(
            tags = emptyList(),
            primaryGenre = "action",
            gameId = "test6",
        )
        assertTrue("Action X should be > 0, got $x", x > 0)
        assertTrue("Action Y should be > 0, got $y", y > 0)
    }

    @Test
    fun `coordinates are clamped to plus or minus 90`() {
        val (x, y) = calculateGameCoordinates(
            tags = listOf("souls_like", "demanding", "survival", "tactical", "deck_building", "stealth",
                "open_world", "sandbox", "roguelike", "puzzle", "rhythm"),
            primaryGenre = "action",
            gameId = "test7",
        )
        assertTrue("X should be clamped to [-90, 90], got $x", x in -90.0..90.0)
        assertTrue("Y should be clamped to [-90, 90], got $y", y in -90.0..90.0)
    }

    @Test
    fun `deterministic jitter produces same result for same ID`() {
        val (x1, y1) = calculateGameCoordinates(emptyList(), "unknown", "game123")
        val (x2, y2) = calculateGameCoordinates(emptyList(), "unknown", "game123")
        assertEquals("X should be deterministic", x1, x2, 0.001)
        assertEquals("Y should be deterministic", y1, y2, 0.001)
    }

    @Test
    fun `different game IDs produce different jitter`() {
        val (x1, _) = calculateGameCoordinates(emptyList(), "unknown", "gameA")
        val (x2, _) = calculateGameCoordinates(emptyList(), "unknown", "gameB")
        assertTrue("Different IDs should produce different jitter", x1 != x2)
    }
}
