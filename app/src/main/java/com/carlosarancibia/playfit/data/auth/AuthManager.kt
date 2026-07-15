package com.carlosarancibia.playfit.data.auth

import com.carlosarancibia.playfit.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class AuthSessionInfo(
    val userId: String,
    val email: String?,
    val isAnonymous: Boolean,
)

sealed class AuthResult {
    data class Success(val session: AuthSessionInfo? = null) : AuthResult()
    data class Pending(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Supabase's password-recovery redirect marks itself with `type=recovery`, either as a query
 * parameter (PKCE-style callback, e.g. `playfit://auth-callback?code=...&type=recovery`) or inside
 * the URL fragment (implicit-style callback, e.g. `playfit://auth-callback#access_token=...&type=recovery`).
 * `Uri.getFragment()` returns the raw fragment string with no built-in param parser, so both the
 * query and the fragment are parsed the same way here. Pure string parsing (no android.net.Uri) so
 * this stays unit-testable on the JVM without Robolectric.
 */
internal fun isPasswordRecoveryLink(query: String?, fragment: String?): Boolean {
    return containsRecoveryType(query) || containsRecoveryType(fragment)
}

private fun containsRecoveryType(rawParams: String?): Boolean {
    if (rawParams.isNullOrEmpty()) return false
    return rawParams.split("&").any { pair ->
        val separator = pair.indexOf('=')
        if (separator <= 0) return@any false
        val key = pair.substring(0, separator)
        val value = pair.substring(separator + 1)
        key == "type" && value == "recovery"
    }
}

@Singleton
class AuthManager @Inject constructor(
    private val deviceIdProvider: DeviceIdProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _session = MutableStateFlow<AuthSessionInfo?>(null)
    val session: StateFlow<AuthSessionInfo?> = _session.asStateFlow()

    private val _pendingPasswordRecovery = MutableStateFlow<UserSession?>(null)
    val pendingPasswordRecovery: StateFlow<UserSession?> = _pendingPasswordRecovery.asStateFlow()

    @Volatile
    private var expectingPasswordRecoverySession = false

    @Volatile
    private var _cachedAccessToken: String? = null

    val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Auth) {
            scheme = "playfit"
            host = "auth-callback"
            autoLoadFromStorage = true
            autoSaveToStorage = true
            alwaysAutoRefresh = true
        }
    }

    val deviceId: String get() = deviceIdProvider.id
    val cachedAccessToken: String? get() = _cachedAccessToken

    init {
        scope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        if (expectingPasswordRecoverySession) {
                            expectingPasswordRecoverySession = false
                            _pendingPasswordRecovery.value = status.session
                        } else {
                            applySession(status.session)
                        }
                    }
                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure -> clearSessionSnapshot()
                    SessionStatus.Initializing -> Unit
                }
            }
        }
    }

    /**
     * Called from MainActivity with the incoming deep-link's raw query and fragment strings,
     * before (or alongside) `supabase.handleDeeplinks(intent)`. If they indicate a password-recovery
     * callback, the next `SessionStatus.Authenticated` emission is routed into
     * [pendingPasswordRecovery] instead of being treated as a normal sign-in.
     */
    fun markPendingPasswordRecovery(query: String?, fragment: String?) {
        if (isPasswordRecoveryLink(query, fragment)) {
            expectingPasswordRecoverySession = true
        }
    }

    /** Dismisses the recovery flow without completing it (e.g. the user cancels the screen). */
    fun cancelPendingPasswordRecovery() {
        expectingPasswordRecoverySession = false
        _pendingPasswordRecovery.value = null
    }

    suspend fun restoreSession(): AuthSessionInfo? {
        supabase.auth.awaitInitialization()
        return supabase.auth.currentSessionOrNull()?.let(::applySession)
    }

    suspend fun signInAnonymously(): AuthResult = authOperation("Anonymous sign-in failed") {
        supabase.auth.signInAnonymously()
        requireCurrentSession()
    }

    suspend fun signInWithGoogle(): AuthResult {
        return try {
            supabase.auth.signInWith(
                provider = Google,
                redirectUrl = BuildConfig.AUTH_REDIRECT_URL,
            )
            AuthResult.Pending("Complete Google sign-in in your browser.")
        } catch (error: Exception) {
            AuthResult.Error(error.message ?: "Google sign-in failed")
        }
    }

    suspend fun linkGoogleIdentity(): AuthResult {
        return try {
            if (_session.value == null) {
                return AuthResult.Error("Sign in before linking a Google identity.")
            }
            supabase.auth.linkIdentity(
                provider = Google,
                redirectUrl = BuildConfig.AUTH_REDIRECT_URL,
            )
            AuthResult.Pending("Complete Google linking in your browser.")
        } catch (error: Exception) {
            AuthResult.Error(error.message ?: "Google linking failed")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult =
        authOperation("Sign in failed. Check your credentials.") {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            requireCurrentSession()
        }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return try {
            supabase.auth.signUpWith(
                provider = Email,
                redirectUrl = BuildConfig.AUTH_REDIRECT_URL,
            ) {
                this.email = email
                this.password = password
            }
            val current = supabase.auth.currentSessionOrNull()
            if (current != null) {
                AuthResult.Success(applySession(current))
            } else {
                AuthResult.Pending("Check your email to verify your account.")
            }
        } catch (error: Exception) {
            AuthResult.Error(error.message ?: "Sign up failed.")
        }
    }

    suspend fun resetPassword(email: String): AuthResult {
        return try {
            supabase.auth.resetPasswordForEmail(
                email = email,
                redirectUrl = BuildConfig.AUTH_REDIRECT_URL,
            )
            AuthResult.Pending("If that email is registered, you'll receive a reset link shortly.")
        } catch (error: Exception) {
            AuthResult.Error(error.message ?: "Password reset failed.")
        }
    }

    suspend fun updatePassword(newPassword: String): AuthResult {
        return try {
            supabase.auth.updateUser {
                password = newPassword
            }
            val recoverySession = _pendingPasswordRecovery.value
            _pendingPasswordRecovery.value = null
            AuthResult.Success(applySession(recoverySession ?: requireCurrentSession()))
        } catch (error: Exception) {
            AuthResult.Error(error.message ?: "Could not update password.")
        }
    }

    suspend fun signOut(): AuthResult {
        return try {
            supabase.auth.signOut()
            clearSessionSnapshot()
            AuthResult.Success()
        } catch (error: Exception) {
            AuthResult.Error(error.message ?: "Sign out failed")
        }
    }

    suspend fun getAccessToken(): String? {
        supabase.auth.awaitInitialization()
        return supabase.auth.currentSessionOrNull()?.let {
            applySession(it)
            it.accessToken
        }
    }

    fun isAuthenticated(): Boolean = _session.value != null

    private suspend fun authOperation(
        fallbackMessage: String,
        operation: suspend () -> UserSession,
    ): AuthResult {
        return try {
            AuthResult.Success(applySession(operation()))
        } catch (error: Exception) {
            AuthResult.Error(error.message ?: fallbackMessage)
        }
    }

    private fun requireCurrentSession(): UserSession {
        return requireNotNull(supabase.auth.currentSessionOrNull()) {
            "Supabase did not return an authenticated session."
        }
    }

    private fun applySession(value: UserSession): AuthSessionInfo {
        _cachedAccessToken = value.accessToken
        val user = requireNotNull(value.user) { "Authenticated session is missing user data." }
        return AuthSessionInfo(
            userId = user.id,
            email = user.email,
            isAnonymous = user.isAnonymous == true,
        ).also { _session.value = it }
    }

    private fun clearSessionSnapshot() {
        _cachedAccessToken = null
        _session.value = null
    }
}
