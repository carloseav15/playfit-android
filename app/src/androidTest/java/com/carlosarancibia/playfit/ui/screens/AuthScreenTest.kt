package com.carlosarancibia.playfit.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.carlosarancibia.playfit.data.auth.AuthResult
import com.carlosarancibia.playfit.ui.theme.PlayfitTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun invalidEmailShowsValidationErrorWithoutCallingBackend() {
        var signInCalled = false
        composeRule.setContent {
            PlayfitTheme {
                AuthScreen(
                    onDismiss = {},
                    onGoogleSignIn = { AuthResult.Success() },
                    onEmailSignIn = { _, _ -> signInCalled = true; AuthResult.Success() },
                    onEmailSignUp = { _, _ -> AuthResult.Success() },
                    onGuestSignIn = { AuthResult.Success() },
                )
            }
        }

        composeRule.onNodeWithText("Continue with Email").performClick()
        composeRule.onNodeWithText("Email Address").performTextInput("not-an-email")
        composeRule.onNodeWithText("Password").performTextInput("123456")
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.onNodeWithText("Please enter a valid email address.").assertIsDisplayed()
        assertFalse(signInCalled)
    }

    @Test
    fun shortPasswordShowsValidationErrorWithoutCallingBackend() {
        var signInCalled = false
        composeRule.setContent {
            PlayfitTheme {
                AuthScreen(
                    onDismiss = {},
                    onGoogleSignIn = { AuthResult.Success() },
                    onEmailSignIn = { _, _ -> signInCalled = true; AuthResult.Success() },
                    onEmailSignUp = { _, _ -> AuthResult.Success() },
                    onGuestSignIn = { AuthResult.Success() },
                )
            }
        }

        composeRule.onNodeWithText("Continue with Email").performClick()
        composeRule.onNodeWithText("Email Address").performTextInput("player@playfit.app")
        composeRule.onNodeWithText("Password").performTextInput("123")
        composeRule.onNodeWithText("Sign In").performClick()

        composeRule.onNodeWithText("Password must be at least 6 characters.").assertIsDisplayed()
        assertFalse(signInCalled)
    }

    @Test
    fun resetPasswordShowsNonEnumerativeMessageOnPending() {
        composeRule.setContent {
            PlayfitTheme {
                AuthScreen(
                    onDismiss = {},
                    onGoogleSignIn = { AuthResult.Success() },
                    onEmailSignIn = { _, _ -> AuthResult.Success() },
                    onEmailSignUp = { _, _ -> AuthResult.Success() },
                    onGuestSignIn = { AuthResult.Success() },
                    onResetPassword = { AuthResult.Pending("Check your email for the password reset link.") },
                )
            }
        }

        composeRule.onNodeWithText("Continue with Email").performClick()
        composeRule.onNodeWithText("Email Address").performTextInput("player@playfit.app")
        composeRule.onNodeWithText("Forgot password?").performClick()

        composeRule.onNodeWithText(PASSWORD_RESET_NON_ENUMERATIVE_MESSAGE).assertIsDisplayed()
    }
}
