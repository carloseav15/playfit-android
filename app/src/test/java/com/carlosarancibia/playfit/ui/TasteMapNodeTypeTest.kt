package com.carlosarancibia.playfit.ui

import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.ProductTasteHistoryEntry
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.ui.screens.NodeType
import com.carlosarancibia.playfit.ui.screens.GameNode
import com.carlosarancibia.playfit.ui.screens.buildMapNodes
import com.carlosarancibia.playfit.ui.screens.toMapNodeType
import org.junit.Assert.assertEquals
import org.junit.Test

class TasteMapNodeTypeTest {

    @Test
    fun `saved pick without rating remains pending`() {
        val state = ProductGameState(
            gameId = "pick-1",
            title = "Pending Pick",
            inPlayfitPicks = true,
        )

        assertEquals(NodeType.Pending, state.toMapNodeType())
    }

    @Test
    fun `positive rating is liked`() {
        val state = ProductGameState(
            gameId = "liked-1",
            title = "Liked Game",
            rating = 4.0,
        )

        assertEquals(NodeType.Liked, state.toMapNodeType())
    }

    @Test
    fun `excluded state is avoided`() {
        val state = ProductGameState(
            gameId = "avoided-1",
            title = "Avoided Game",
            excluded = true,
            rating = 5.0,
        )

        assertEquals(NodeType.Avoided, state.toMapNodeType())
    }

    @Test
    fun `map nodes prefer hydrated taste history metadata`() {
        val nodes = buildMapNodes(
            state = ProductState(),
            picks = emptyList(),
            playNext = null,
            tasteModel = ProductTasteModel(
                historyEntries = listOf(
                    ProductTasteHistoryEntry(
                        gameId = "history-1",
                        title = "Hydrated Game",
                        decision = "liked",
                        source = "rating",
                        tone = "positive",
                        traits = listOf("story_rich"),
                        coverUrl = "https://example.com/cover.jpg",
                    ),
                ),
            ),
        )

        assertEquals(1, nodes.size)
        assertEquals("Hydrated Game", nodes.first().title)
        assertEquals(NodeType.Liked, nodes.first().type)
        assertEquals("https://example.com/cover.jpg", nodes.first().coverUrl)
    }
}
