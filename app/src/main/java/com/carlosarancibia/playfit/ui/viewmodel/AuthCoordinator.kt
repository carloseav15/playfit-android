package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.auth.AuthResult
import com.carlosarancibia.playfit.data.auth.AuthSessionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isAnonymous: Boolean = false,
    val userId: String? = null,
    val email: String? = null,
    val canLinkGoogle: Boolean = false,
    val canSignOut: Boolean = false,
    val canDeleteAccount: Boolean = false,
)

internal class AuthCoordinator(
    private val authManager: AuthManager,
    private val scope: CoroutineScope,
    private val setToast: (String) -> Unit,
    private val setError: (String) -> Unit,
) {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun observeSession() {
        scope.launch {
            authManager.session.collect { session ->
                _state.value = session.toAuthState()
            }
        }
    }

    suspend fun restoreOrCreateAnonymousSession() {
        val restored = try {
            authManager.restoreSession()
        } catch (_: Exception) {
            null
        }
        if (restored != null) return

        when (val result = authManager.signInAnonymously()) {
            is AuthResult.Error -> setError(result.message)
            is AuthResult.Pending -> setToast(result.message)
            is AuthResult.Success -> Unit
        }
    }

    fun linkGoogleAccount() {
        scope.launch {
            when (val result = authManager.linkGoogleIdentity()) {
                is AuthResult.Success -> setToast("Google account linked.")
                is AuthResult.Pending -> setToast(result.message)
                is AuthResult.Error -> setToast(result.message)
            }
        }
    }

    fun signOutAsync() {
        scope.launch {
            when (val result = authManager.signOut()) {
                is AuthResult.Error -> setToast(result.message)
                is AuthResult.Pending -> setToast(result.message)
                is AuthResult.Success -> setToast("Signed out.")
            }
        }
    }

    fun deleteAccount() {
        if (!_state.value.canDeleteAccount) {
            setToast("Cloud account deletion is not available in the Android app yet.")
            return
        }
        scope.launch {
            when (val result = authManager.deleteAccount()) {
                is AuthResult.Error -> setToast(result.message)
                is AuthResult.Pending -> setToast(result.message)
                is AuthResult.Success -> setToast("Account deleted.")
            }
        }
    }

    suspend fun signInAnonymously(): AuthResult = authManager.signInAnonymously()
    suspend fun signInWithGoogle(): AuthResult = authManager.signInWithGoogle()
    suspend fun signInWithEmail(email: String, password: String): AuthResult =
        authManager.signInWithEmail(email, password)
    suspend fun signUpWithEmail(email: String, password: String): AuthResult =
        authManager.signUpWithEmail(email, password)
    suspend fun resetPassword(email: String): AuthResult = authManager.resetPassword(email)
    suspend fun signInAsGuest(): AuthResult = authManager.signInAnonymously()
    suspend fun signOut(): AuthResult = authManager.signOut()

    private fun AuthSessionInfo?.toAuthState(): AuthState = AuthState(
        isAuthenticated = this != null,
        isAnonymous = this?.isAnonymous == true,
        userId = this?.userId,
        email = this?.email,
        canLinkGoogle = this?.isAnonymous == true,
        canSignOut = this != null,
        canDeleteAccount = false,
    )
}
