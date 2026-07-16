package com.carlosarancibia.playfit.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.carlosarancibia.playfit.ui.theme.PlayfitTheme
import org.junit.Rule
import org.junit.Test

class AppChromeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBarExposesEveryTopLevelDestination() {
        composeRule.setContent {
            PlayfitTheme {
                PlayfitBottomBar(
                    currentRoute = "play-next",
                    pickCount = 3,
                    onNavigate = {},
                )
            }
        }

        listOf("Play Next", "Picks", "Taste", "Search", "Settings").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
        composeRule.onNodeWithContentDescription("Picks").assertIsDisplayed()
    }

    @Test
    fun syncStatusCommunicatesPendingOfflineChanges() {
        composeRule.setContent {
            PlayfitTheme {
                SyncStatusBar(
                    refreshing = false,
                    saving = false,
                    pendingSync = true,
                    showingStaleData = false,
                )
            }
        }

        composeRule.onNodeWithText("Changes saved on this device; waiting to sync").assertIsDisplayed()
    }
}
