package com.carlosarancibia.playfit.data.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordRecoveryLinkTest {

    @Test
    fun `detects recovery type in query params`() {
        assertTrue(isPasswordRecoveryLink(query = "code=abc123&type=recovery", fragment = null))
    }

    @Test
    fun `detects recovery type in fragment params`() {
        assertTrue(
            isPasswordRecoveryLink(
                query = null,
                fragment = "access_token=xyz&type=recovery&refresh_token=abc",
            ),
        )
    }

    @Test
    fun `ignores non-recovery type values`() {
        assertFalse(isPasswordRecoveryLink(query = "type=signup", fragment = null))
        assertFalse(isPasswordRecoveryLink(query = null, fragment = "type=magiclink"))
    }

    @Test
    fun `returns false when both query and fragment are absent`() {
        assertFalse(isPasswordRecoveryLink(query = null, fragment = null))
    }

    @Test
    fun `returns false for blank or malformed params`() {
        assertFalse(isPasswordRecoveryLink(query = "", fragment = ""))
        assertFalse(isPasswordRecoveryLink(query = "type", fragment = null))
        assertFalse(isPasswordRecoveryLink(query = "=recovery", fragment = null))
    }

    @Test
    fun `is order independent among multiple params`() {
        assertTrue(isPasswordRecoveryLink(query = "type=recovery&code=abc123", fragment = null))
    }
}
