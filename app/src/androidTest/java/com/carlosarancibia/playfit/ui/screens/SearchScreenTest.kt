package com.carlosarancibia.playfit.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.ui.theme.PlayfitTheme
import com.carlosarancibia.playfit.ui.viewmodel.SearchUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val platforms = listOf(
        Platform("switch_2", "Nintendo Switch 2", "nintendo", "hybrid"),
        Platform("ps5", "PlayStation 5", "playstation", "console"),
    )

    @Test
    fun typingInvokesQueryChange() {
        var lastQuery: String? = null
        composeRule.setContent {
            PlayfitTheme {
                SearchScreen(
                    searchState = SearchUiState(),
                    platforms = platforms,
                    onQueryChange = { lastQuery = it },
                    onFamilyChange = {},
                    onLoadMore = {},
                    onRetry = {},
                    onOpenGame = {},
                )
            }
        }

        composeRule.onNodeWithText("Search by title...").performTextInput("mario")
        assertEquals("mario", lastQuery)
    }

    @Test
    fun tappingPlatformChipInvokesFamilyChange() {
        var lastFamily: String? = "unset"
        composeRule.setContent {
            PlayfitTheme {
                SearchScreen(
                    searchState = SearchUiState(),
                    platforms = platforms,
                    onQueryChange = {},
                    onFamilyChange = { lastFamily = it },
                    onLoadMore = {},
                    onRetry = {},
                    onOpenGame = {},
                )
            }
        }

        composeRule.onNodeWithText("Nintendo").performClick()
        assertEquals("nintendo", lastFamily)
    }

    @Test
    fun errorStateShowsMessageAndRetryInvokesCallback() {
        var retried = false
        composeRule.setContent {
            PlayfitTheme {
                SearchScreen(
                    searchState = SearchUiState(query = "mario", error = "Search could not load. Try again."),
                    platforms = platforms,
                    onQueryChange = {},
                    onFamilyChange = {},
                    onLoadMore = {},
                    onRetry = { retried = true },
                    onOpenGame = {},
                )
            }
        }

        composeRule.onNodeWithText("Search could not load. Try again.").assertIsDisplayed()
        composeRule.onNodeWithText("Try again").performClick()
        assertEquals(true, retried)
    }

    @Test
    fun loadMoreOnlyShowsWhenHasMoreAndInvokesCallback() {
        var loadedMore = false
        composeRule.setContent {
            PlayfitTheme {
                SearchScreen(
                    searchState = SearchUiState(
                        query = "zelda",
                        results = listOf(SeedGame(gameId = "z1", title = "Zelda 1")),
                        total = 2,
                    ),
                    platforms = platforms,
                    onQueryChange = {},
                    onFamilyChange = {},
                    onLoadMore = { loadedMore = true },
                    onRetry = {},
                    onOpenGame = {},
                )
            }
        }

        composeRule.onNodeWithText("Load more").performClick()
        assertEquals(true, loadedMore)
    }

    @Test
    fun tappingResultInvokesOpenGameWithId() {
        var openedGameId: String? = null
        composeRule.setContent {
            PlayfitTheme {
                SearchScreen(
                    searchState = SearchUiState(
                        query = "zelda",
                        results = listOf(SeedGame(gameId = "z1", title = "Zelda 1")),
                        total = 1,
                    ),
                    platforms = platforms,
                    onQueryChange = {},
                    onFamilyChange = {},
                    onLoadMore = {},
                    onRetry = {},
                    onOpenGame = { openedGameId = it },
                )
            }
        }

        composeRule.onNodeWithText("Zelda 1").performClick()
        assertEquals("z1", openedGameId)
    }
}
