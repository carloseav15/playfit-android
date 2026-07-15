package com.carlosarancibia.playfit.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.carlosarancibia.playfit.model.ProductTasteHistoryEntry
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.ui.theme.PlayfitTheme
import org.junit.Rule
import org.junit.Test

class DecisionsActivityScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun pickEntry() = ProductTasteHistoryEntry(
        gameId = "pick-1",
        title = "Picked Game",
        decision = "picks",
        source = "manual",
        tone = "info",
    )

    private fun signalEntry() = ProductTasteHistoryEntry(
        gameId = "signal-1",
        title = "Rated Game",
        decision = "liked",
        source = "played",
        tone = "positive",
    )

    @Test
    fun pickEntryOnlyOffersRemoveFromPicks() {
        composeRule.setContent {
            PlayfitTheme {
                DecisionsActivityContent(
                    tasteModel = ProductTasteModel(historyEntries = listOf(pickEntry())),
                    onOpenGame = {},
                    onRemovePick = {},
                    onChangeSignal = { _, _ -> },
                    onDeleteSignal = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Manage signal for Picked Game").performClick()

        composeRule.onNodeWithText("Remove from Picks").assertIsDisplayed()
        composeRule.onNodeWithText("Change Signal").assertDoesNotExist()
        composeRule.onNodeWithText("Delete Signal").assertDoesNotExist()
    }

    @Test
    fun signalEntryOffersChangeAndDeleteButNotRemoveFromPicks() {
        composeRule.setContent {
            PlayfitTheme {
                DecisionsActivityContent(
                    tasteModel = ProductTasteModel(historyEntries = listOf(signalEntry())),
                    onOpenGame = {},
                    onRemovePick = {},
                    onChangeSignal = { _, _ -> },
                    onDeleteSignal = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Manage signal for Rated Game").performClick()

        composeRule.onNodeWithText("Change Signal").assertIsDisplayed()
        composeRule.onNodeWithText("Delete Signal").assertIsDisplayed()
        composeRule.onNodeWithText("Remove from Picks").assertDoesNotExist()
    }
}
