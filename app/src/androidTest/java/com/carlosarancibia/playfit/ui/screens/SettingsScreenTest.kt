package com.carlosarancibia.playfit.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.carlosarancibia.playfit.ui.theme.PlayfitTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsWithoutProfileShowsCalibrationCallToAction() {
        composeRule.setContent {
            PlayfitTheme {
                SettingsScreen(
                    viewModel = null,
                    hasProfile = false,
                )
            }
        }

        composeRule.onNodeWithText("Set up your taste first").assertIsDisplayed()
        composeRule.onNodeWithText("Start Play Next").assertIsDisplayed()
    }
}
