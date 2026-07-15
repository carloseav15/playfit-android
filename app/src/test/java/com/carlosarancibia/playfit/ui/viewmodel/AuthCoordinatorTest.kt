package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.auth.AuthResult
import com.carlosarancibia.playfit.data.auth.AuthSessionInfo
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthCoordinatorTest {
    private lateinit var authManager: AuthManager
    private lateinit var session: MutableStateFlow<AuthSessionInfo?>
    private lateinit var toasts: MutableList<String>
    private lateinit var errors: MutableList<String>

    @Before
    fun setUp() {
        authManager = mockk()
        session = MutableStateFlow(null)
        toasts = mutableListOf()
        errors = mutableListOf()
        every { authManager.session } returns session
        every { authManager.pendingPasswordRecovery } returns MutableStateFlow<UserSession?>(null)
    }

    @Test
    fun `observed session maps anonymous and signed out auth state`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val coordinator = coordinator(scope)
        coordinator.observeSession()

        session.value = AuthSessionInfo("guest", null, isAnonymous = true)
        assertTrue(coordinator.state.value.isAuthenticated)
        assertTrue(coordinator.state.value.isAnonymous)
        assertTrue(coordinator.state.value.canLinkGoogle)
        assertTrue(coordinator.state.value.canDeleteAccount)

        session.value = null
        assertFalse(coordinator.state.value.isAuthenticated)
        assertFalse(coordinator.state.value.canSignOut)
        assertFalse(coordinator.state.value.canDeleteAccount)
        scope.cancel()
    }

    @Test
    fun `restored session avoids creating a guest session`() = runTest {
        val restored = AuthSessionInfo("user", "user@example.com", isAnonymous = false)
        coEvery { authManager.restoreSession() } returns restored
        val coordinator = coordinator(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        coordinator.restoreOrCreateAnonymousSession()

        coVerify { authManager.restoreSession() }
        coVerify(exactly = 0) { authManager.signInAnonymously() }
    }

    @Test
    fun `missing session creates a guest and reports failure`() = runTest {
        coEvery { authManager.restoreSession() } returns null
        coEvery { authManager.signInAnonymously() } returns AuthResult.Error("Offline")
        val coordinator = coordinator(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        coordinator.restoreOrCreateAnonymousSession()

        coVerify { authManager.signInAnonymously() }
        assertEquals(listOf("Offline"), errors)
    }

    @Test
    fun `link and sign out report their outcomes`() = runTest {
        coEvery { authManager.linkGoogleIdentity() } returns AuthResult.Pending("Continue in browser")
        coEvery { authManager.signOut() } returns AuthResult.Success()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val coordinator = coordinator(scope)

        coordinator.linkGoogleAccount()
        coordinator.signOutAsync()
        assertEquals(setOf("Continue in browser", "Signed out."), toasts.toSet())
        scope.cancel()
    }

    private fun coordinator(scope: CoroutineScope) = AuthCoordinator(
        authManager = authManager,
        scope = scope,
        setToast = toasts::add,
        setError = errors::add,
    )
}
