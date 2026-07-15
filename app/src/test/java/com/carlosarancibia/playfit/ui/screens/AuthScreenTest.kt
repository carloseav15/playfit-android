package com.carlosarancibia.playfit.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthScreenTest {

    @Test
    fun `blank email is rejected`() {
        assertEquals(
            "Please enter a valid email address.",
            validateAuthInput(email = "", password = "hunter22"),
        )
    }

    @Test
    fun `email without at sign is rejected`() {
        assertEquals(
            "Please enter a valid email address.",
            validateAuthInput(email = "not-an-email", password = "hunter22"),
        )
    }

    @Test
    fun `password shorter than 6 characters is rejected`() {
        assertEquals(
            "Password must be at least 6 characters.",
            validateAuthInput(email = "player@playfit.app", password = "12345"),
        )
    }

    @Test
    fun `valid email and password pass validation`() {
        assertNull(validateAuthInput(email = "player@playfit.app", password = "123456"))
    }

    @Test
    fun `password reset copy stays non-enumerative`() {
        assertEquals(
            "If that email is registered, you'll receive a reset link shortly.",
            PASSWORD_RESET_NON_ENUMERATIVE_MESSAGE,
        )
    }
}
